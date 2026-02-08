package reservation_service.implementation;

import api.core.reservation.Reservation;
import api.core.reservation.ReservationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reservation_service.mapper.ReservationMapper;
import reservation_service.repository.ReservationRepository;

@RestController
public class ReservationServiceImplementation implements ReservationService {

    private final ReservationRepository repo;
    private final ReservationMapper mapper;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ReservationServiceImplementation(ReservationRepository repo, ReservationMapper mapper, RabbitTemplate rabbitTemplate) {
        this.repo = repo;
        this.mapper = mapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Mono<Reservation> createReservation(Reservation body) {
        return repo.save(mapper.apiToEntity(body))
                .doOnSuccess(saved -> {
                    Mono.fromRunnable(() -> {
                        try {
                            NotificationRequest notification = new NotificationRequest(
                                body.getUserEmail(),
                                "Reservation Confirmed",
                                "Your reservation for court " + body.getCourtNumber() + " is confirmed."
                            );
                            rabbitTemplate.convertAndSend("notification-queue", notification);
                            System.out.println(">>> Notification sent for reservation: " + saved.getId());
                        } catch (Exception e) {
                            System.err.println(">>> FAILED to send notification: " + e.getMessage());
                        }
                    })
                    .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                    .subscribe();
                })
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<Reservation> getReservations(String email) {
        if (email != null && !email.isEmpty()) {
            return repo.findByUserEmail(email).map(mapper::entityToApi);
        }
        return repo.findAll().map(mapper::entityToApi);
    }

    @Override
    public Mono<Void> deleteReservation(int id) {
        return repo.deleteById(id);
    }

    public static class NotificationRequest {
        public String recipientEmail;
        public String subject;
        public String message;

        public NotificationRequest(String recipientEmail, String subject, String message) {
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.message = message;
        }
        public String getRecipientEmail() { return recipientEmail; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
    }
}