CREATE TABLE IF NOT EXISTS review_model (
    id SERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    club_id INTEGER NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    review_date DATE NOT NULL
);