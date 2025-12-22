package review_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import review_service.dto.ReviewDto;
import review_service.implementation.ReviewServiceImplementation;
import review_service.model.ReviewModel;
import review_service.repository.ReviewRepository;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository repo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ReviewServiceImplementation reviewService;

    @Test
    void addReview_Success() {
        ReviewDto dto = new ReviewDto(0, "user@test.com", 1, 5, "Great", LocalDate.now());
        ReviewModel savedModel = new ReviewModel("user@test.com", 1, 5, "Great", LocalDate.now());
        savedModel.setId(1);

        when(restTemplate.getForEntity(contains("user-service"), eq(Object.class)))
            .thenReturn(ResponseEntity.ok().build());
        when(restTemplate.getForEntity(contains("club-service"), eq(Object.class)))
            .thenReturn(ResponseEntity.ok().build());
        
        when(repo.save(any(ReviewModel.class))).thenReturn(savedModel);

        ResponseEntity<?> response = reviewService.addReview(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(repo, times(1)).save(any(ReviewModel.class));
    }

    @Test
    void addReview_Fail_InvalidRating() {
        ReviewDto dto = new ReviewDto(0, "user@test.com", 1, 6, "Bad Rating", LocalDate.now());
        
        assertThrows(InvalidRequestException.class, () -> {
            reviewService.addReview(dto);
        });
    }

    @Test
    void addReview_Fail_UserNotFound() {
        ReviewDto dto = new ReviewDto(0, "unknown@test.com", 1, 5, "Comment", LocalDate.now());

        when(restTemplate.getForEntity(contains("user-service"), eq(Object.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(NoDataFoundException.class, () -> {
            reviewService.addReview(dto);
        });
    }
}