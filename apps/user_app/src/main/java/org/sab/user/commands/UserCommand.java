package org.sab.user.commands;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.sab.user.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.Schema;
import org.sab.service.Command;
import org.sab.user.RequestVerificationException;


public abstract class UserCommand extends Command {
    protected static final String USERNAME = "username";
    protected static final String EMAIL = "email";
    protected static final String PASSWORD = "password";
    protected static final String BIRTHDATE = "birthdate";
    protected static final String PHOTO_URL = "photoUrl";


    protected JSONObject body;
    protected Schema schema;
    @Override
    public final String execute(JSONObject request) {
        body = request.getJSONObject("body");
        schema = getSchema();
        try {
            verifyBody();
        } catch (RequestVerificationException e) {
            return Responder.makeErrorResponse(e.getMessage(), 400).toString();
        }
        return execute();
    }

    //abstract methods
    protected abstract String execute();

    protected abstract Schema getSchema();

    //instance methods
    private boolean isFoundInBody(Attribute attribute) {
        return body.keySet().contains(attribute.getAttributeName());
    }

    private void verifyBody() throws RequestVerificationException {
        checkForMissingAttributes();
        checkForInvalidlyTypedAttributes();
    }
    
    private void checkForMissingAttributes() throws RequestVerificationException {

        final Predicate<Attribute> isNotFoundInBody = attribute -> !isFoundInBody(attribute);

        final List<Attribute> missingAttributes = schema.getAttributeList().stream().filter(isNotFoundInBody)
                .filter(Attribute::isRequired).collect(Collectors.toList());

        if (!missingAttributes.isEmpty()) {
            final String exceptionMessage = "Some attributes were missing: "
                    + missingAttributes.stream().map(Attribute::getAttributeName).collect(Collectors.joining(", "));
            throw new RequestVerificationException(exceptionMessage);
        }

    }
    
    private void checkForInvalidlyTypedAttributes() throws RequestVerificationException {
        final Predicate<Attribute> isInvalidlyTyped = attribute -> !attribute.isValidlyTyped();

        final List<Attribute> invalidlyTypedAttributes = schema.getAttributeList().stream().filter(this::isFoundInBody)
                .filter(isInvalidlyTyped).collect(Collectors.toList());

        if (!invalidlyTypedAttributes.isEmpty()) {
            final String exceptionMessage = invalidlyTypedAttributes.stream().map(this::makeInvalidlyTypedAttributeMessage)
                    .collect(Collectors.joining("\n "));
            throw new RequestVerificationException(exceptionMessage);
        }
    }

    private String makeInvalidlyTypedAttributeMessage(Attribute attribute) {
        return String.format("%s must be of type %s.%s", attribute.getAttributeName(),
                attribute.getDataType().toString(), attribute.getDataType().getAdditionalErrorMessage());
    }



}
