package api.core.notification;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface NotificationService {
    
    @PostMapping(value = "/notification", consumes = "application/json", produces = "application/json")
    Mono<Notification> sendNotification(@RequestBody Notification body);
}
