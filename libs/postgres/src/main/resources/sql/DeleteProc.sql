drop FUNCTION create_user(user_id VARCHAR,
                                       username   VARCHAR,
                                       email      VARCHAR,
                                       password   VARCHAR,
                                       birthdate      date,
                                       photo_url VARCHAR);


DROP FUNCTION delete_user(in_username VARCHAR);


DROP FUNCTION update_user_password(in_username VARCHAR, new_password VARCHAR);

DROP FUNCTION get_user(in_username VARCHAR);


DROP FUNCTION update_profile_picture(in_username VARCHAR, new_photo_url VARCHAR);


DROP FUNCTION delete_profile_picture(in_username VARCHAR);
