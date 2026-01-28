INSERT INTO user_model(email, password, role)
VALUES 
    ('admin@uns.ac.rs', '$2a$12$oxQN5StnTwLFfTQgZSM7QuCE9mP08Z6LyDTZu5FxJP1UvjJrErWBe', 'ADMIN'),
    ('user@uns.ac.rs', '$2a$12$oxQN5StnTwLFfTQgZSM7QuCE9mP08Z6LyDTZu5FxJP1UvjJrErWBe', 'USER'),
    ('owner@uns.ac.rs', '$2a$12$oxQN5StnTwLFfTQgZSM7QuCE9mP08Z6LyDTZu5FxJP1UvjJrErWBe', 'OWNER'),
    ('milos@uns.ac.rs', '$2a$12$oxQN5StnTwLFfTQgZSM7QuCE9mP08Z6LyDTZu5FxJP1UvjJrErWBe', 'USER')
ON CONFLICT (email) DO NOTHING;