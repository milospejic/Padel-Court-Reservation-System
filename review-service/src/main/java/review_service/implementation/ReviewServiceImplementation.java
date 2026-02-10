package review_service.implementation;

import api.core.review.Review;
import api.core.review.ReviewService;
import api.event.ClubDeletedEvent;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import review_service.mapper.ReviewMapper;
import review_service.repository.ReviewRepository;
import util.exceptions.*;

@RestController
public class ReviewServiceImplementation implements ReviewService {

    private final ReviewRepository repo;
    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImplementation(ReviewRepository repo, ReviewMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @GetMapping(value = "/review", produces = "application/json")
    @Override
    public Flux<Review> getReviews(@RequestParam(value = "clubId", required = true) int clubId) {
        return repo.findByClubId(clubId).map(mapper::entityToApi);
    }

    @PostMapping(value = "/review", consumes = "application/json", produces = "application/json")
    public Mono<Review> createReview(@RequestBody Review body, ServerWebExchange exchange) {
        String currentUserEmail = exchange.getRequest().getHeaders().getFirst("logged-in-user-id");
        if (currentUserEmail == null) return Mono.error(new ForbidenActionException("Identity missing"));

        body.setUserEmail(currentUserEmail);

        if (body.getClubId() <= 0) return Mono.error(new InvalidRequestException("Invalid clubId"));
        if (body.getRating() < 1 || body.getRating() > 5) return Mono.error(new InvalidRequestException("Invalid rating"));

        return repo.existsByClubIdAndUserEmail(body.getClubId(), currentUserEmail)
            .flatMap(exists -> {
                if (exists) return Mono.error(new EntityAlreadyExistsException("Review already exists for this club"));
                return repo.save(mapper.apiToEntity(body)).map(mapper::entityToApi);
            });
    }

    @Override
    public Mono<Review> createReview(Review body) {
        return Mono.error(new InvalidRequestException("Internal Error: Method requires context"));
    }

    @DeleteMapping(value = "/review/{id}")
    public Mono<Void> deleteReview(@PathVariable int id, ServerWebExchange exchange) {
        String currentUserEmail = exchange.getRequest().getHeaders().getFirst("logged-in-user-id");

        return repo.findById(id)
            .switchIfEmpty(Mono.error(new NoDataFoundException("Review not found: " + id)))
            .flatMap(review -> {
                if (!review.getUserEmail().equals(currentUserEmail)) {
                    return Mono.error(new ForbidenActionException("You can only delete your own reviews."));
                }
                return repo.delete(review);
            });
    }

    @Override
    public Mono<Void> deleteReview(int id) {
        return Mono.error(new InvalidRequestException("Internal Error: Method requires context"));
    }
    
    @RabbitListener(queues = "review-cleanup-queue")
    public void handleClubDeletedEvent(ClubDeletedEvent event) {
        System.out.println("Received ClubDeletedEvent for Club ID: " + event.getClubId());
        repo.deleteByClubId(event.getClubId()).subscribe();
    }
}