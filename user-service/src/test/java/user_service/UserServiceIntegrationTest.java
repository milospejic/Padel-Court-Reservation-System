package user_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import user_service.dto.UserDto;
import user_service.model.UserModel;
import user_service.repository.UserServiceRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserServiceRepository repo;

    private String validAdminAuth;

    @BeforeEach
    void setUp() {
        repo.deleteAll().block();
        
        repo.save(new UserModel("admin@uns.ac.rs", "password", "ADMIN")).block();
        
        String auth = "admin@uns.ac.rs:password";
        validAdminAuth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Test
    void createUser_Flow() {
        UserDto newUser = new UserDto("test@uns.ac.rs", "123", "USER");

        webTestClient.post().uri("/user")
                .header("Authorization", validAdminAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo("test@uns.ac.rs");

        webTestClient.get().uri("/user/email/test@uns.ac.rs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.role").isEqualTo("USER");
    }

    @Test
    void createUser_Forbidden_AsAdmin() {
        UserDto newAdmin = new UserDto("otheradmin@uns.ac.rs", "123", "ADMIN");

        webTestClient.post().uri("/user")
                .header("Authorization", validAdminAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newAdmin)
                .exchange()
                .expectStatus().isForbidden();
    }
}