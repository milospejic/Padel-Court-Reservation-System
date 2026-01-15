package review_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import api.core.review.Review;
import review_service.implementation.ReviewServiceImplementation;
import review_service.model.ReviewModel;
import review_service.repository.ReviewRepository;
import util.exceptions.InvalidRequestException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository repo;

    @InjectMocks
    private ReviewServiceImplementation reviewService;

    @Test
    void addReview_Success() {
        Review review = new Review(0, 1, "user@test.com", 5, "Great", LocalDate.now(), null);
        
        ReviewModel savedModel = new ReviewModel("user@test.com", 1, 5, "Great", LocalDate.now());
        savedModel.setId(1);

        when(repo.save(any(ReviewModel.class))).thenReturn(Mono.just(savedModel));

        Review response = reviewService.createReview(review).block();

        assertNotNull(response);
        assertEquals(5, response.getRating());
        verify(repo, times(1)).save(any(ReviewModel.class));
    }

    @Test
    void addReview_Fail_InvalidRating() {
        Review review = new Review(0, 1, "user@test.com", 6, "Bad Rating", LocalDate.now(), null);
        
        assertThrows(InvalidRequestException.class, () -> {
            reviewService.createReview(review).block();
        });
    }
}