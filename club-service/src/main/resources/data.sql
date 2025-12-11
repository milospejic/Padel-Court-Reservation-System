INSERT INTO club_model (name, location, phone_number) 
VALUES 
    ('Padel Pro Center', 'Bulevar Oslobodjenja 12, Novi Sad', '+381641234567'),
    ('Sunny Courts', 'Futoska 55, Novi Sad', '+381609876543'),
    ('Ace Padel Club', 'Zeleznicka 10, Novi Sad', '+381611122334')
ON CONFLICT (name) DO NOTHING;