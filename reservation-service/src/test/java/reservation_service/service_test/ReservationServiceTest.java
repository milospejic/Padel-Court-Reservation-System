package reservation_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reservation_service.dto.ReservationDto;
import reservation_service.implementation.ReservationServiceImplementation;
import reservation_service.model.ReservationModel;
import reservation_service.repository.ReservationRepository;
import util.exceptions.InvalidRequestException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository repo;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ReservationServiceImplementation reservationService;

    @BeforeEach
    void setUpWebClientMock() {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void createReservation_Success() {
        ReservationDto dto = new ReservationDto(0, "user@test.com", 1, 1, LocalDateTime.now().plusDays(1));
        ReservationModel savedModel = new ReservationModel("user@test.com", 1, 1, dto.getReservationTime());
        savedModel.setId(100);


        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.just(new Object()));

        when(repo.save(any(ReservationModel.class))).thenReturn(Mono.just(savedModel));

        ResponseEntity<?> response = reservationService.createReservation(dto).block();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(repo, times(1)).save(any(ReservationModel.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("notification-queue"), any(Object.class));
    }

    @Test
    void createReservation_Fail_UserNotFound() {
        ReservationDto dto = new ReservationDto(0, "unknown@test.com", 1, 1, LocalDateTime.now());
        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.error(new RuntimeException("User not found")));
        assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(dto).block();
        });
        
        verify(repo, times(0)).save(any(ReservationModel.class));
    }
}