CREATE OR REPLACE PROCEDURE Insert_User(
 	IN username varchar(255),
  IN email varchar(500),
 	IN password varchar(255),
  IN birthdate date,
  IN hasprofilephoto boolean,
  user_id varchar(50)
)
LANGUAGE SQL
AS $$
  INSERT INTO users (username,email, password, birthdate, hasprofilephoto,user_id) 
    VALUES (username,email, password, birthdate, hasprofilephoto,user_id);
$$;