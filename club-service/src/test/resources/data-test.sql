-- Clean up before inserting to prevent duplicates on context reload
DELETE FROM club_model;

INSERT INTO club_model (name, location, phone_number) 
VALUES ('Padel Pro Center', 'Bulevar Oslobodjenja 12, Novi Sad', '+381641234567');

INSERT INTO club_model (name, location, phone_number) 
VALUES ('Sunny Courts', 'Bulevar Evrope 5, Novi Sad', '+381691112233');