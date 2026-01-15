package notification_service.service_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import reactor.core.publisher.Mono;

import api.core.notification.Notification;
import notification_service.implementation.NotificationServiceImplementation;
import notification_service.model.NotificationModel;
import notification_service.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository repo;

    @InjectMocks
    private NotificationServiceImplementation notificationService;

    @Test
    void sendNotification_Success() {
        Notification notification = new Notification("user@test.com", "Subject", "Message", LocalDateTime.now(), null);
        
        when(repo.save(any(NotificationModel.class))).thenReturn(Mono.just(new NotificationModel()));

        Notification response = notificationService.sendNotification(notification).block();

        assertNotNull(response);
        verify(repo, times(1)).save(any(NotificationModel.class));
    }
}