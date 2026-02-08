package notification_service.mapper;

import api.core.notification.Notification;
import notification_service.model.NotificationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.http.ServiceUtil;
import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    private final ServiceUtil serviceUtil;

    @Autowired
    public NotificationMapper(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public Notification entityToApi(NotificationModel entity) {
        return new Notification(
            entity.getRecipientEmail(),
            entity.getSubject(),
            entity.getMessage(),
            entity.getSentAt(),
            serviceUtil.getServiceAddress()
        );
    }

    public NotificationModel apiToEntity(Notification api) {
        return new NotificationModel(
            api.getRecipientEmail(),
            api.getSubject(),
            api.getMessage(),
            (api.getSentAt() != null) ? api.getSentAt() : LocalDateTime.now()
        );
    }
}