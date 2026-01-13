CREATE TABLE IF NOT EXISTS club_model (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    location VARCHAR(255),
    phone_number VARCHAR(50)
);