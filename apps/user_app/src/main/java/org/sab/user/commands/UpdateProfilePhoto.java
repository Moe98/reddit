package org.sab.user.commands;

import org.sab.min_io.MinIO;
import org.sab.models.user.User;
import org.sab.models.user.UserAttributes;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.SQLException;
import java.util.List;

public class UpdateProfilePhoto extends UserCommand {
    @Override
    protected Schema getSchema() {
        return new Schema(List.of());

    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {
        boolean authenticated = authenticationParams.getBoolean(AUTHENTICATED);
        if (!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);
        if(files.length()!=1)
            return Responder.makeErrorResponse("One profile image is only allowed per upload, Check Form-Data Files!", 400);

        // retrieving the body objects
        String username = authenticationParams.getString(USERNAME);
        String photo = files.getJSONObject("image").getString("data");
        String contentType = files.getJSONObject("image").getString("type");
        String output = "";
        User user;
        // getting the user
        try {
            user = getUser(username, UserAttributes.USER_ID);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }
        String publicId =  user.getUserId().replaceAll("[-]", "");

        try {
//            photoUrl = CloudinaryUtilities.uploadImage(photoUrl, user.getUserId());
          output =  MinIO.uploadObject("profile-picture-scaleabull",publicId,photo,contentType);
          System.out.println(output);
          if(output.isEmpty()){
              return Responder.makeErrorResponse("Error Occurred While Uploading Your Image!", 404);
          }
        } catch (EnvironmentVariableNotLoaded e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        }


        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("update_profile_picture", username, output);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeMsgResponse("Profile Picture uploaded successfully");
    }
}
