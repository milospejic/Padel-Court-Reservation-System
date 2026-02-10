package review_service;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import api.core.review.Review;
import review_service.repository.ReviewRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReviewServiceIntegrationTest {

    @Autowired private WebTestClient webTestClient;
    @Autowired private ReviewRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll().block();
    }

    @Test
    void addReview_Success() {
        Review review = new Review(0, 1, "ignored", 5, "Great court!", LocalDate.now(), null);

        webTestClient.post().uri("/review")
                .header("logged-in-user-id", "user@test.com")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(review)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.comment").isEqualTo("Great court!");
    }
}