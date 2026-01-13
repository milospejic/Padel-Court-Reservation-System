package review_service.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import review_service.dto.ReviewDto;

public interface ReviewService {

    @PostMapping("/review")
    Mono<ResponseEntity<?>> addReview(@RequestBody ReviewDto dto);

    @GetMapping("/review/club/{clubId}")
    Flux<ReviewDto> getReviewsByClub(@PathVariable int clubId);

    @GetMapping("/review/user/{email}")
    Flux<ReviewDto> getReviewsByUser(@PathVariable String email);

    @DeleteMapping("/review/{id}")
    Mono<ResponseEntity<?>> deleteReview(@PathVariable int id);
}