package reservation_service.service_test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import reservation_service.dto.ReservationDto;
import reservation_service.implementation.ReservationServiceImplementation;
import reservation_service.model.ReservationModel;
import reservation_service.repository.ReservationRepository;
import util.exceptions.InvalidRequestException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repo;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReservationServiceImplementation reservationService;

    @Test
    void createReservation_Success() {
        ReservationDto dto = new ReservationDto(0, "user@test.com", 1, 1, LocalDateTime.now().plusDays(1));
        ReservationModel savedModel = new ReservationModel("user@test.com", 1, 1, dto.getReservationTime());
        savedModel.setId(100);

        when(restTemplate.getForEntity(contains("user-service"), eq(Object.class)))
            .thenReturn(ResponseEntity.ok().build());
        when(restTemplate.getForEntity(contains("club-service"), eq(Object.class)))
            .thenReturn(ResponseEntity.ok().build());

        when(repo.save(any(ReservationModel.class))).thenReturn(savedModel);

        ResponseEntity<?> response = reservationService.createReservation(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        verify(repo, times(1)).save(any(ReservationModel.class));
        
        verify(rabbitTemplate, times(1)).convertAndSend(eq("notification-queue"), any(Object.class));
    }

    @Test
    void createReservation_Fail_UserNotFound() {
        ReservationDto dto = new ReservationDto(0, "unknown@test.com", 1, 1, LocalDateTime.now());

        when(restTemplate.getForEntity(contains("user-service"), eq(Object.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(InvalidRequestException.class, () -> {
            reservationService.createReservation(dto);
        });
        
        verify(rabbitTemplate, times(0)).convertAndSend(anyString(), any(Object.class));
    }
}