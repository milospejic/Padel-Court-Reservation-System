package notification_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import notification_service.model.NotificationModel;
import reactor.core.publisher.Flux;

public interface NotificationRepository extends ReactiveCrudRepository<NotificationModel, Integer> {
    
    Flux<NotificationModel> findByRecipientEmail(String recipientEmail);
}