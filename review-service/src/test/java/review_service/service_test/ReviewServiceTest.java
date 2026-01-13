package review_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import review_service.dto.ReviewDto;
import review_service.implementation.ReviewServiceImplementation;
import review_service.model.ReviewModel;
import review_service.repository.ReviewRepository;
import util.exceptions.InvalidRequestException;
import util.exceptions.NoDataFoundException;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository repo;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ReviewServiceImplementation reviewService;

    @BeforeEach
    void setUpWebClientMock() {
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void addReview_Success() {
        ReviewDto dto = new ReviewDto(0, "user@test.com", 1, 5, "Great", LocalDate.now());
        ReviewModel savedModel = new ReviewModel("user@test.com", 1, 5, "Great", LocalDate.now());
        savedModel.setId(1);

        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.just(new Object()));
        
        when(repo.save(any(ReviewModel.class))).thenReturn(Mono.just(savedModel));

        ResponseEntity<?> response = reviewService.addReview(dto).block();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(repo, times(1)).save(any(ReviewModel.class));
    }

    @Test
    void addReview_Fail_InvalidRating() {
        ReviewDto dto = new ReviewDto(0, "user@test.com", 1, 6, "Bad Rating", LocalDate.now());
        
        assertThrows(InvalidRequestException.class, () -> {
            reviewService.addReview(dto).block();
        });
    }
}