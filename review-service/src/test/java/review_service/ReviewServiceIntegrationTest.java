package review_service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import review_service.dto.ReviewDto;
import review_service.repository.ReviewRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReviewServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewRepository repo;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    void setUp() {
        repo.deleteAll().block();

        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.just(new Object()));
    }

    @Test
    void addReview_Success() {
        ReviewDto review = new ReviewDto(0, "user@test.com", 1, 5, "Great court!", LocalDate.now());

        webTestClient.post().uri("/review")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.comment").isEqualTo("Great court!");
    }
}