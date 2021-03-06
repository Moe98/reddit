CREATE OR REPLACE FUNCTION create_user(user_id VARCHAR,
                                       username   VARCHAR,
                                       email      VARCHAR,
                                       password   VARCHAR,
                                       birthdate      date,
                                       photo_url VARCHAR DEFAULT NULL)
    RETURNS SETOF users AS $$
BEGIN
    INSERT INTO users (user_id,username, email, password,birthdate,photo_url)
    VALUES (user_id,username, email, password, birthdate, photo_url);
END; $$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION delete_user(in_username VARCHAR)
    RETURNS VOID AS $$
BEGIN
    DELETE FROM users
    WHERE username = in_username ;

    INSERT INTO deleted_users (username)
    VALUES (in_username);
END; $$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION update_user_password(in_username VARCHAR, new_password VARCHAR)
    RETURNS VOID AS $$
BEGIN

    UPDATE users
    SET password = new_password
    WHERE username = in_username ;
END; $$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION get_user(in_username VARCHAR)
    RETURNS Table(user_id VARCHAR, username VARCHAR, email VARCHAR,birthdate date,photo_url text,password VARCHAR) AS $$
    BEGIN
        Return Query (Select u.user_id,u.username,u.email ,u.birthdate, u.photo_url,u.password from users u WHERE u.username = in_username);
    END; $$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION update_profile_picture(in_username VARCHAR, new_photo_url text)
    RETURNS VOID AS $$
BEGIN

    UPDATE users
    SET photo_url = new_photo_url
    WHERE username = in_username ;
END; $$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION delete_profile_picture(in_username VARCHAR)
    RETURNS VOID AS $$
BEGIN

    UPDATE users
    SET photo_url = NULL
    WHERE username = in_username ;
END; $$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION is_username_deleted(in_username VARCHAR)
    RETURNS bool
AS
$$
declare
    Successful bool;
BEGIN
    SELECT count(*) AS is_username_deleted INTO Successful FROM deleted_users where username = in_username;
    return Successful;
END;
$$
    LANGUAGE PLPGSQL;
