CREATE TABLE IF NOT EXISTS notification_model (
    id SERIAL PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT,
    sent_at TIMESTAMP
);