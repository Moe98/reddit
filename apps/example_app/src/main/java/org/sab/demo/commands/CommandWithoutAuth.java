package org.sab.demo.commands;

import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

public class CommandWithoutAuth extends CommandWithVerification {


    // isAuthNeeded is by default false

    @Override
    protected String execute() {
        return Responder.makeMsgResponse("I don't need authentication!");
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
