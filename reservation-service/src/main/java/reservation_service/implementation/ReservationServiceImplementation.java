package reservation_service.implementation;

import api.core.reservation.Reservation;
import api.core.reservation.ReservationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reservation_service.model.ReservationModel;
import reservation_service.repository.ReservationRepository;

@RestController
public class ReservationServiceImplementation implements ReservationService {

    @Autowired
    private ReservationRepository repo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public Mono<Reservation> createReservation(Reservation body) {
        return repo.save(apiToEntity(body))
                .doOnSuccess(saved -> {
                    NotificationRequest notification = new NotificationRequest(
                        body.getUserEmail(),
                        "Reservation Confirmed",
                        "Your reservation for court " + body.getCourtNumber() + " is confirmed."
                    );
                    rabbitTemplate.convertAndSend("notification-queue", notification);
                })
                .map(this::entityToApi);
    }

    @Override
    public Flux<Reservation> getReservations(String email) {
        if (email != null && !email.isEmpty()) {
            return repo.findByUserEmail(email).map(this::entityToApi);
        }
        return repo.findAll().map(this::entityToApi);
    }

    @Override
    public Mono<Void> deleteReservation(int id) {
        return repo.deleteById(id);
    }

    private Reservation entityToApi(ReservationModel entity) {
        return new Reservation(
            entity.getId(),
            entity.getUserEmail(),
            entity.getClubId(),
            entity.getCourtNumber(),
            entity.getReservationTime(),
            null
        );
    }

    private ReservationModel apiToEntity(Reservation api) {
        return new ReservationModel(
            api.getUserEmail(),
            api.getClubId(),
            api.getCourtNumber(),
            api.getReservationTime()
        );
    }

    public static class NotificationRequest {
        public String recipient;
        public String subject;
        public String message;
        public NotificationRequest(String recipient, String subject, String message) {
            this.recipient = recipient; this.subject = subject; this.message = message;
        }
        public String getRecipient() { return recipient; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
    }
}