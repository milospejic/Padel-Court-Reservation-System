package notification_service.service_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import notification_service.dto.NotificationDto;
import notification_service.implementation.NotificationServiceImplementation;
import notification_service.model.NotificationModel;
import notification_service.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repo;

    @InjectMocks
    private NotificationServiceImplementation notificationService;

    @Test
    void sendNotification_Success() {
        NotificationDto dto = new NotificationDto("user@test.com", "Subject", "Message");
        dto.setSentAt(LocalDateTime.now());
        
        when(repo.save(any(NotificationModel.class))).thenReturn(new NotificationModel());

        ResponseEntity<?> response = notificationService.sendNotification(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(repo, times(1)).save(any(NotificationModel.class));
    }
}