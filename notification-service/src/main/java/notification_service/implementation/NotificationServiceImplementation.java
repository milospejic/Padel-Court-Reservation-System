package notification_service.implementation;

import api.core.notification.Notification;
import api.core.notification.NotificationService;
import notification_service.mapper.NotificationMapper;
import notification_service.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class NotificationServiceImplementation implements NotificationService {

    private final NotificationRepository repo;
    private final NotificationMapper mapper;

    @Autowired
    public NotificationServiceImplementation(NotificationRepository repo, NotificationMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Mono<Notification> sendNotification(Notification body) {
        return repo.save(mapper.apiToEntity(body))
                .map(mapper::entityToApi);
    }

    @RabbitListener(queues = "notification-queue")
    public void handleNotificationMessage(Notification body) {
        System.out.println(">>> Received Async Notification Message!");
        repo.save(mapper.apiToEntity(body)).subscribe();
    }
}