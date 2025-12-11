package review_service.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import review_service.dto.ReviewDto;

public interface ReviewService {

    @PostMapping("/review")
    ResponseEntity<?> addReview(@RequestBody ReviewDto dto);

    @GetMapping("/review/club/{clubId}")
    List<ReviewDto> getReviewsByClub(@PathVariable int clubId);

    @GetMapping("/review/user/{email}")
    List<ReviewDto> getReviewsByUser(@PathVariable String email);

    @DeleteMapping("/review/{id}")
    ResponseEntity<?> deleteReview(@PathVariable int id);
}