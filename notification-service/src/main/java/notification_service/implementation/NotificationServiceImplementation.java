package notification_service.implementation;

import java.time.LocalDateTime;
import notification_service.dto.NotificationDto;
import notification_service.model.NotificationModel;
import notification_service.repository.NotificationRepository;
import notification_service.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class NotificationServiceImplementation implements NotificationService {

    @Autowired
    private NotificationRepository repo;

    @Override
    public Mono<ResponseEntity<?>> sendNotification(@RequestBody NotificationDto dto) {
        return processNotification(dto)
                .then(Mono.just(ResponseEntity.status(HttpStatus.OK).body("Notification sent via REST.")));
    }


    @RabbitListener(queues = "notification-queue")
    public void handleNotificationMessage(NotificationDto dto) {
        System.out.println(">>> Received Async Notification Message!");
        processNotification(dto).subscribe();
    }

    private Mono<NotificationModel> processNotification(NotificationDto dto) {
        System.out.println("------------------------------------------------");
        System.out.println("SENDING EMAIL TO: " + dto.getRecipientEmail());
        System.out.println("SUBJECT: " + dto.getSubject());
        System.out.println("BODY: " + dto.getMessage());
        System.out.println("------------------------------------------------");

        NotificationModel model = new NotificationModel(
            dto.getRecipientEmail(),
            dto.getSubject(),
            dto.getMessage(),
            LocalDateTime.now()
        );
        return repo.save(model);
    }
}