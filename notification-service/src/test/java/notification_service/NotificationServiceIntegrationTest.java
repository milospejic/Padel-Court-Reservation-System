package notification_service;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import api.core.notification.Notification;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class NotificationServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void sendNotification_Success() {
        Notification notification = new Notification("user@test.com", "Test Subject", "Hello World", LocalDateTime.now(), null);

        webTestClient.post().uri("/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notification)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Notification.class);
    }
}