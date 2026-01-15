package notification_service.implementation;

import api.core.notification.Notification;
import api.core.notification.NotificationService;
import notification_service.model.NotificationModel;
import notification_service.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
public class NotificationServiceImplementation implements NotificationService {

    @Autowired
    private NotificationRepository repo;

    @Override
    public Mono<Notification> sendNotification(Notification body) {
        return processNotification(body).map(this::entityToApi);
    }

    @RabbitListener(queues = "notification-queue")
    public void handleNotificationMessage(Notification body) {
        System.out.println(">>> Received Async Notification Message!");
        processNotification(body).subscribe();
    }

    private Mono<NotificationModel> processNotification(Notification body) {
        NotificationModel model = new NotificationModel(
            body.getRecipientEmail(),
            body.getSubject(),
            body.getMessage(),
            (body.getSentAt() != null) ? body.getSentAt() : LocalDateTime.now()
        );
        return repo.save(model);
    }

    private Notification entityToApi(NotificationModel entity) {
        return new Notification(
            entity.getRecipientEmail(),
            entity.getSubject(),
            entity.getMessage(),
            entity.getSentAt(),
            null
        );
    }
}