package org.sab.demo.commands;

import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

public class CommandNeedingClaims extends CommandWithVerification {

    @Override
    protected String execute() {
        String username = authenticationParams.getString("username");
        return Responder.makeMsgResponse(String.format("Hello %s!", username));
    }

    @Override
    protected Schema getSchema() {
        return Schema.emptySchema();
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected boolean isAuthNeeded(){
        return true;
    }
}
