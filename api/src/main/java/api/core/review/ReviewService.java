package api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {
    Flux<Review> getReviews(@RequestParam(value = "clubId", required = true) int clubId);

    Mono<Review> createReview(@RequestBody Review body);

    Mono<Void> deleteReview(@PathVariable int id);
}