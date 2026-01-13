DELETE FROM user_model;

INSERT INTO user_model (email, password, role) 
VALUES ('user@uns.ac.rs', '123', 'USER');

INSERT INTO user_model (email, password, role) 
VALUES ('admin@uns.ac.rs', 'admin', 'ADMIN');