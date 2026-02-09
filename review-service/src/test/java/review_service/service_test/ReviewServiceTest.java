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
import review_service.mapper.ReviewMapper;
import review_service.model.ReviewModel;
import review_service.repository.ReviewRepository;
import util.exceptions.InvalidRequestException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock private ReviewRepository repo;
    @Mock private ReviewMapper reviewMapper; // Add this

    @InjectMocks
    private ReviewServiceImplementation reviewService;

    @Test
    void addReview_Success() {
        Review apiReview = new Review(0, 1, "u@u.com", 5, "Nice", null, null);
        ReviewModel model = new ReviewModel();

        when(reviewMapper.apiToEntity(any())).thenReturn(model);
        when(repo.save(any())).thenReturn(Mono.just(model));
        when(reviewMapper.entityToApi(any())).thenReturn(apiReview);

        Review response = reviewService.createReview(apiReview).block();

        assertNotNull(response);
        assertEquals(5, response.getRating());
    }


    @Test
    void addReview_Fail_InvalidRating() {
        Review review = new Review(0, 1, "user@test.com", 6, "Bad Rating", LocalDate.now(), null);
        
        assertThrows(InvalidRequestException.class, () -> {
            reviewService.createReview(review).block();
        });
    }
}