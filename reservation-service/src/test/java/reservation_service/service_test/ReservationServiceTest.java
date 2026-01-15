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

import reactor.core.publisher.Mono;
import api.core.reservation.Reservation;
import reservation_service.implementation.ReservationServiceImplementation;
import reservation_service.model.ReservationModel;
import reservation_service.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository repo;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReservationServiceImplementation reservationService;

    @Test
    void createReservation_Success() {
        Reservation res = new Reservation(0, "user@test.com", 1, 1, LocalDateTime.now().plusDays(1), null);
        
        ReservationModel savedModel = new ReservationModel("user@test.com", 1, 1, res.getReservationTime());
        savedModel.setId(100);

        when(repo.save(any(ReservationModel.class))).thenReturn(Mono.just(savedModel));

        Reservation response = reservationService.createReservation(res).block();

        assertNotNull(response);
        assertEquals(100, response.getId());
        verify(repo, times(1)).save(any(ReservationModel.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("notification-queue"), any(Object.class));
    }
}