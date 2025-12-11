package notification_service.dto;

import java.time.LocalDateTime;
import java.io.Serializable;

public class NotificationDto implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String recipientEmail;
    private String subject;
    private String message;
    private LocalDateTime sentAt;

    public NotificationDto() {}

    public NotificationDto(String recipientEmail, String subject, String message) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
    }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}