package reservation_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import api.core.reservation.Reservation;
import reservation_service.implementation.ReservationServiceImplementation;
import reservation_service.mapper.ReservationMapper;
import reservation_service.model.ReservationModel;
import reservation_service.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    @Mock private ReservationRepository repo;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ReservationMapper reservationMapper; 

    @InjectMocks
    private ReservationServiceImplementation reservationService;

    @Test
    void createReservation_Success() {
        Reservation res = new Reservation(0, "ignored", 1, 1, LocalDateTime.now(), null);
        ReservationModel model = new ReservationModel();

        // MOCK HEADER
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
            .header("logged-in-user-id", "user@test.com")
            .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(reservationMapper.apiToEntity(any())).thenReturn(model);
        when(repo.save(any())).thenReturn(Mono.just(model));
        when(reservationMapper.entityToApi(any())).thenReturn(res);

        // Call method with exchange
        Reservation response = reservationService.createReservation(res, exchange).block();

        assertNotNull(response);
        verify(rabbitTemplate).convertAndSend(eq("notification-queue"), any(Object.class));
    }
}