package reservation_service.implementation;

import api.core.reservation.Reservation;
import api.core.reservation.ReservationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reservation_service.mapper.ReservationMapper;
import reservation_service.repository.ReservationRepository;
import util.exceptions.ForbidenActionException;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

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

    @PostMapping(value = "/reservation", consumes = "application/json", produces = "application/json")
    public Mono<Reservation> createReservation(@RequestBody Reservation body, ServerWebExchange exchange) {
        String currentUserEmail = exchange.getRequest().getHeaders().getFirst("logged-in-user-id");
        if (currentUserEmail == null) return Mono.error(new ForbidenActionException("Identity missing"));

        body.setUserEmail(currentUserEmail);

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
                        } catch (Exception e) {}
                    }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()).subscribe();
                })
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Reservation> createReservation(Reservation body) {
        return Mono.error(new InvalidRequestException("Internal Error: Method requires context"));
    }

    @GetMapping(value = "/reservation", produces = "application/json")
    @Override
    public Flux<Reservation> getReservations(@RequestParam(value = "email", required = false) String email) {
        return Flux.deferContextual(ctx -> {
            ServerWebExchange exchange = ctx.getOrDefault(ServerWebExchange.class, null);
            String requesterRole = (exchange != null) ? exchange.getRequest().getHeaders().getFirst("logged-in-user-role") : null;
            String requesterId = (exchange != null) ? exchange.getRequest().getHeaders().getFirst("logged-in-user-id") : null;


            if ("USER".equals(requesterRole)) {
                if (requesterId == null) return Flux.error(new ForbidenActionException("Identity missing"));
                return repo.findByUserEmail(requesterId).map(mapper::entityToApi);
            }

            if ("ADMIN".equals(requesterRole)) {
                if (email != null && !email.isEmpty()) {
                    return repo.findByUserEmail(email).map(mapper::entityToApi);
                }
                return repo.findAll().map(mapper::entityToApi);
            }
            
            return Flux.error(new ForbidenActionException("Access denied"));
        });
    }

 
    @DeleteMapping(value = "/reservation/{id}")
    public Mono<Void> deleteReservation(@PathVariable int id, ServerWebExchange exchange) {
        String currentUserEmail = exchange.getRequest().getHeaders().getFirst("logged-in-user-id");
        if (currentUserEmail == null) return Mono.error(new ForbidenActionException("Identity missing"));

        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("Reservation not found: " + id)))
                .flatMap(res -> {
                    if (!res.getUserEmail().equals(currentUserEmail)) {
                        return Mono.error(new ForbidenActionException("You can only delete your own reservations."));
                    }
                    return repo.delete(res);
                });
    }

    @Override
    public Mono<Void> deleteReservation(int id) {
        return Mono.error(new InvalidRequestException("Internal Error: Method requires context"));
    }

    public static class NotificationRequest {
        public String recipientEmail;
        public String subject;
        public String message;
        public NotificationRequest(String r, String s, String m) { this.recipientEmail = r; this.subject = s; this.message = m; }
        public String getRecipientEmail() { return recipientEmail; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
    }
}