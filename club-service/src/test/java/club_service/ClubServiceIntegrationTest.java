package club_service;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import club_service.dto.ClubDto;
import club_service.repository.ClubRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ClubServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ClubRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll().block();
    }

    @Test
    void createAndGetClub_Flow() {
        ClubDto newClub = new ClubDto("Integration Club", "Test City", "12345");

        webTestClient.post().uri("/club")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newClub)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get().uri("/club")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[?(@.name == 'Integration Club')]").exists();
    }

    @Test
    void getClub_NotFound() {
        webTestClient.get().uri("/club/9999")
                .exchange()
                .expectStatus().isNotFound();
    }
}