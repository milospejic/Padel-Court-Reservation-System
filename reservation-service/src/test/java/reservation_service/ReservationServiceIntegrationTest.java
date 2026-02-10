package reservation_service;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import api.core.reservation.Reservation;
import reservation_service.repository.ReservationRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReservationServiceIntegrationTest {

    @Autowired private WebTestClient webTestClient;
    @Autowired private ReservationRepository repo;
    @MockBean private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        repo.deleteAll().block();
    }

    @Test
    void createReservation_Success() {
        Reservation reservation = new Reservation(0, "ignored", 1, 3, LocalDateTime.now().plusDays(1), null);

        webTestClient.post().uri("/reservation")
                .header("logged-in-user-id", "user@test.com") 
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reservation)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.courtNumber").isEqualTo(3);
    }
}