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
import notification_service.mapper.NotificationMapper;
import notification_service.model.NotificationModel;
import notification_service.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock private NotificationRepository repo;
    @Mock private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImplementation notificationService;

    @Test
    void sendNotification_Success() {
        Notification apiNotif = new Notification("user@test.com", "Sub", "Msg", null, null);
        NotificationModel model = new NotificationModel();

        when(notificationMapper.apiToEntity(any())).thenReturn(model);
        when(repo.save(any())).thenReturn(Mono.just(model));
        when(notificationMapper.entityToApi(any())).thenReturn(apiNotif);

        Notification response = notificationService.sendNotification(apiNotif).block();

        assertNotNull(response);
        verify(notificationMapper).apiToEntity(apiNotif);
    }
}