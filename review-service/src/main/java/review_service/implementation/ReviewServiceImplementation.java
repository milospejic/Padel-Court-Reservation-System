package review_service.implementation;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import review_service.dto.ReviewDto;
import review_service.model.ReviewModel;
import review_service.repository.ReviewRepository;
import review_service.service.ReviewService;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

@RestController
public class ReviewServiceImplementation implements ReviewService {

    @Autowired
    private ReviewRepository repo;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Override
    @CircuitBreaker(name = "reviewService", fallbackMethod = "addReviewFallback")
    public Mono<ResponseEntity<?>> addReview(ReviewDto dto) {
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            return Mono.error(new InvalidRequestException("Rating must be between 1 and 5"));
        }

        WebClient webClient = webClientBuilder.build();

        Mono<Object> userCheck = webClient.get()
                .uri("http://user-service/user/email/" + dto.getUserEmail())
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorMap(e -> new NoDataFoundException("User email not found: " + dto.getUserEmail()));

        Mono<Object> clubCheck = webClient.get()
                .uri("http://club-service/club/" + dto.getClubId())
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorMap(e -> new NoDataFoundException("Club ID not found: " + dto.getClubId()));

        return Mono.zip(userCheck, clubCheck)
                .flatMap(tuple -> {
                    ReviewModel model = new ReviewModel(
                        dto.getUserEmail(),
                        dto.getClubId(),
                        dto.getRating(),
                        dto.getComment(),
                        LocalDate.now()
                    );
                    return repo.save(model);
                })
                .map(saved -> {
                    dto.setId(saved.getId());
                    dto.setReviewDate(saved.getReviewDate());
                    return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.CREATED).body(dto);
                });
    }

    public Mono<ResponseEntity<?>> addReviewFallback(ReviewDto dto, Throwable t) {
        return Mono.just((ResponseEntity<?>) ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Review Service Unavailable: " + t.getMessage()));
    }

    @Override
    public Flux<ReviewDto> getReviewsByClub(int clubId) {
        return repo.findByClubId(clubId)
                .map(this::convertToDto);
    }

    @Override
    public Flux<ReviewDto> getReviewsByUser(String email) {
        return repo.findByUserEmail(email)
                .map(this::convertToDto);
    }

    @Override
    public Mono<ResponseEntity<?>> deleteReview(int id) {
        return repo.existsById(id)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return repo.deleteById(id)
                                .then(Mono.just((ResponseEntity<?>) ResponseEntity.ok("Review deleted successfully")));
                    }
                    return Mono.error(new NoDataFoundException("Review not found with id " + id));
                });
    }

    private ReviewDto convertToDto(ReviewModel m) {
        return new ReviewDto(m.getId(), m.getUserEmail(), m.getClubId(), m.getRating(), m.getComment(), m.getReviewDate());
    }
}