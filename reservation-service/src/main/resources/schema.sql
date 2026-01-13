CREATE TABLE IF NOT EXISTS reservation_model (
    id SERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    club_id INTEGER NOT NULL,
    court_number INTEGER NOT NULL,
    reservation_time TIMESTAMP NOT NULL
);