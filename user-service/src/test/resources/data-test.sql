DELETE FROM user_model;

INSERT INTO user_model (email, password, role) 
VALUES ('user@uns.ac.rs', '$2a$12$oxQN5StnTwLFfTQgZSM7QuCE9mP08Z6LyDTZu5FxJP1UvjJrErWBe', 'USER');

INSERT INTO user_model (email, password, role) 
VALUES ('admin@uns.ac.rs', '$2a$12$oxQN5StnTwLFfTQgZSM7QuCE9mP08Z6LyDTZu5FxJP1UvjJrErWBe', 'ADMIN');