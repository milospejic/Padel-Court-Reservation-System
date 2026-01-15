package user_service;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import api.core.user.User;
import user_service.repository.UserServiceRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired private WebTestClient webTestClient;
    @Autowired private UserServiceRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll().block();
    }

    @Test
    void createUser_Flow() {
        User newUser = new User(0, "test@uns.ac.rs", "123", "USER", null);

        webTestClient.post().uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("test@uns.ac.rs");

        webTestClient.get().uri("/user/email/test@uns.ac.rs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.role").isEqualTo("USER");
    }
}