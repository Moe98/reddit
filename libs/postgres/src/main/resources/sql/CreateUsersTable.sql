CREATE TABLE IF NOT EXISTS users
(
    username character varying(255) PRIMARY KEY,
    email character varying(500) UNIQUE,
    password character varying(255),
    birthdate date,
    photo_url text,
    user_id character varying(50) UNIQUE
);
