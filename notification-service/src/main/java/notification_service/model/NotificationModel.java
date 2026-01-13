package notification_service.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("notification_model")
public class NotificationModel {

    @Id
    private Integer id;

    @Column("recipient_email")
    private String recipientEmail;

    @Column("subject")
    private String subject;

    @Column("message")
    private String message;

    private LocalDateTime sentAt;

    public NotificationModel() {}

    public NotificationModel(String recipientEmail, String subject, String message, LocalDateTime sentAt) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.sentAt = sentAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}