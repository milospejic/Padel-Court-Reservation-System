package notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import notification_service.model.NotificationModel;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationModel, Integer> {
    List<NotificationModel> findByRecipientEmail(String recipientEmail);
}