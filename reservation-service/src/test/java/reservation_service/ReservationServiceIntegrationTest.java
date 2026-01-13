package reservation_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock; // FIXED: Added this import
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reservation_service.dto.ReservationDto;
import reservation_service.repository.ReservationRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReservationServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReservationRepository repo;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @MockBean
    private RabbitTemplate rabbitTemplate;

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
    void createReservation_Success() {
        ReservationDto reservation = new ReservationDto(0, "user@test.com", 1, 3, LocalDateTime.now().plusDays(1));

        webTestClient.post().uri("/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reservation)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.courtNumber").isEqualTo(3);
    }
}