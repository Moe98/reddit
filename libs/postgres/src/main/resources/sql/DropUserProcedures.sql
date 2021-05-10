DROP FUNCTION IF EXISTS create_user (user_id VARCHAR,
                                       username   VARCHAR,
                                       email      VARCHAR,
                                       password   VARCHAR,
                                       birthdate      date,
                                       photo_url VARCHAR);


DROP FUNCTION IF EXISTS delete_user(in_username VARCHAR);


DROP FUNCTION IF EXISTS update_user_password(in_username VARCHAR, new_password VARCHAR);

DROP FUNCTION IF EXISTS get_user(in_username VARCHAR);


DROP FUNCTION IF EXISTS update_profile_picture(in_username VARCHAR, new_photo_url text);


DROP FUNCTION IF EXISTS delete_profile_picture(in_username VARCHAR);
