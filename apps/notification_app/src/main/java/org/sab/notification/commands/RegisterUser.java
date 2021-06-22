package org.sab.notification.commands;

import org.sab.models.user.UserAttributes;
import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class RegisterUser extends CommandWithVerification {

    static final String USERNAME = UserAttributes.USERNAME.toString();

    @Override
    protected String execute() {
        String username = body.getString(USERNAME);
        // TODO
        System.out.println(username);
        return Responder.makeMsgResponse("You have successfully registered to the notification service");
    }

    @Override
    protected Schema getSchema() {
        final Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        return new Schema(List.of(username));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }
}
