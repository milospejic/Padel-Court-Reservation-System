package api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {
    @GetMapping(value = "/review", produces = "application/json")
    Flux<Review> getReviews(@RequestParam(value = "clubId", required = true) int clubId);

    @PostMapping(value = "/review", consumes = "application/json", produces = "application/json")
    Mono<Review> createReview(@RequestBody Review body);

    @DeleteMapping(value = "/review/{id}")
    Mono<Void> deleteReview(@PathVariable int id);
}