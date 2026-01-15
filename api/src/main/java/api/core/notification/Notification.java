package api.core.notification;

import java.time.LocalDateTime;

public class Notification {
    private String recipientEmail;
    private String subject;
    private String message;
    private LocalDateTime sentAt;
    private String serviceAddress;

    public Notification() {}

    public Notification(String recipientEmail, String subject, String message, LocalDateTime sentAt, String serviceAddress) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.sentAt = sentAt;
        this.serviceAddress = serviceAddress;
    }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getServiceAddress() { return serviceAddress; }
    public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }
}