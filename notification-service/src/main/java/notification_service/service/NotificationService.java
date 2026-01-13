package notification_service.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import notification_service.dto.NotificationDto;
import reactor.core.publisher.Mono;

public interface NotificationService {

    @PostMapping("/notification")
    Mono<ResponseEntity<?>> sendNotification(@RequestBody NotificationDto dto);
}