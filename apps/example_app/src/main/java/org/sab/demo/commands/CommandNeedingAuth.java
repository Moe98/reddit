package org.sab.demo.commands;

import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

public class CommandNeedingAuth extends CommandWithVerification {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected String execute() {
        return Responder.makeMsgResponse("Authentication successful!");
    }

    @Override
    protected Schema getSchema() {
        return Schema.emptySchema();
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }
}
