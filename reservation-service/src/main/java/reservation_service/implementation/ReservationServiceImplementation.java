package reservation_service.implementation;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reservation_service.dto.ReservationDto;
import reservation_service.model.ReservationModel;
import reservation_service.repository.ReservationRepository;
import reservation_service.service.ReservationService;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

@RestController
public class ReservationServiceImplementation implements ReservationService {

    @Autowired
    private ReservationRepository repo;

    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private RabbitTemplate rabbitTemplate; 

    @Override
    @CircuitBreaker(name = "reservationService", fallbackMethod = "createReservationFallback")
    public Mono<ResponseEntity<?>> createReservation(ReservationDto dto) {
        WebClient webClient = webClientBuilder.build();

        Mono<Object> userCheck = webClient.get()
                .uri("http://user-service/user/email/" + dto.getUserEmail())
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorMap(e -> new InvalidRequestException("User email not found: " + dto.getUserEmail()));

        Mono<Object> clubCheck = webClient.get()
                .uri("http://club-service/club/" + dto.getClubId())
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorMap(e -> new InvalidRequestException("Club ID not found: " + dto.getClubId()));

        return Mono.zip(userCheck, clubCheck)
                .flatMap(tuple -> {
                    ReservationModel model = new ReservationModel(
                        dto.getUserEmail(), 
                        dto.getClubId(), 
                        dto.getCourtNumber(), 
                        dto.getReservationTime()
                    );
                    return repo.save(model);
                })
                .doOnSuccess(saved -> {
                    NotificationRequest notification = new NotificationRequest(
                        dto.getUserEmail(),
                        "Reservation Confirmed",
                        "Your reservation for court " + dto.getCourtNumber() + " is confirmed."
                    );
                    rabbitTemplate.convertAndSend("notification-queue", notification); 
                })
                .map(saved -> {
                    dto.setId(saved.getId());
                    return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.CREATED).body(dto);
                });
    }
    
    public Mono<ResponseEntity<?>> createReservationFallback(ReservationDto dto, Throwable t) {
        return Mono.just((ResponseEntity<?>) ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Reservation Service Unavailable: " + t.getMessage()));
    }

    @Override
    public Mono<ResponseEntity<?>> getReservation(int id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NoDataFoundException("Reservation not found")))
                .map(this::convertToDto)
                .map(dto -> (ResponseEntity<?>) ResponseEntity.ok(dto));
    }

    @Override
    public Mono<ResponseEntity<Flux<ReservationDto>>> getReservationsByUser(String email) {
        Flux<ReservationDto> flux = repo.findByUserEmail(email)
                .map(this::convertToDto);
        return Mono.just(ResponseEntity.ok(flux));
    }

    @Override
    public Mono<ResponseEntity<?>> deleteReservation(int id) {
        return repo.existsById(id)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return repo.deleteById(id)
                                .then(Mono.just((ResponseEntity<?>) ResponseEntity.ok("Reservation deleted")));
                    }
                    return Mono.error(new NoDataFoundException("Reservation not found"));
                });
    }

    private ReservationDto convertToDto(ReservationModel m) {
        return new ReservationDto(m.getId(), m.getUserEmail(), m.getClubId(), m.getCourtNumber(), m.getReservationTime());
    }
    
    public static class NotificationRequest {
        public String recipient;
        public String subject;
        public String message;
        
        public NotificationRequest(String recipient, String subject, String message) {
            this.recipient = recipient;
            this.subject = subject;
            this.message = message;
        }
        public String getRecipient() { return recipient; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }    
    }
}