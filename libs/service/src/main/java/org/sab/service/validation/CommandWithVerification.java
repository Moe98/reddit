package org.sab.service.validation;

import org.json.JSONObject;
import org.sab.service.Command;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.Schema;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class CommandWithVerification extends Command {

    protected JSONObject body, uriParams, authenticationParams, files;
    protected Schema schema;
    protected static final String IS_AUTHENTICATED = "isAuthenticated";
    protected JSONObject origRequest;

    @Override
    public final String execute(JSONObject request) {
        origRequest = request;
        schema = getSchema();
        uriParams = request.getJSONObject("uriParams");
        HTTPMethod methodType = getMethodType();
        if (!methodType.equals(request.getString("methodType")))
            return Responder.makeErrorResponse(String.format("%s expects a %s Request!", getClass().getSimpleName(), methodType), 500);
        body = request.has("body") ? request.getJSONObject("body") : new JSONObject();
        authenticationParams = request.has("authenticationParams") ? request.getJSONObject("authenticationParams") : new JSONObject();
        files = request.has("files") ? request.getJSONObject("files") : new JSONObject();
        if (isAuthNeeded() && !authenticationParams.getBoolean(IS_AUTHENTICATED))
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);
        try {
            verifyBody();
        } catch (RequestVerificationException e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        }
        return execute();
    }

    //abstract methods
    protected abstract String execute();

    protected abstract Schema getSchema();

    protected abstract HTTPMethod getMethodType();

    //instance methods
    protected boolean isAuthNeeded() {
        return false;
    }

    private boolean isFoundInBody(Attribute attribute) {
        return isFoundInBody(attribute.getAttributeName());
    }

    protected boolean isFoundInBody(String attribute) {
        return body.has(attribute);
    }

    private void verifyBody() throws RequestVerificationException {
        checkForMissingAttributes();
        checkForInvalidlyTypedAttributes();
    }

    private void checkForMissingAttributes() throws RequestVerificationException {

        final Predicate<Attribute> isMissing = attribute -> !isFoundInBody(attribute);

        final List<Attribute> missingRequiredAttributes = schema.getAttributeList().stream().filter(isMissing)
                .filter(Attribute::isRequired).collect(Collectors.toList());

        if (!missingRequiredAttributes.isEmpty()) {
            final String exceptionMessage = "Some attributes were missing: "
                    + missingRequiredAttributes.stream().map(Attribute::getAttributeName).collect(Collectors.joining(", "));
            throw new RequestVerificationException(exceptionMessage);
        }

    }

    private void checkForInvalidlyTypedAttributes() throws RequestVerificationException {
        final Predicate<Attribute> isInvalidlyTyped = attribute -> !attribute.isValidlyTyped(body.get(attribute.getAttributeName()));

        final List<Attribute> invalidlyTypedAttributes = schema.getAttributeList().stream().filter(this::isFoundInBody)
                .filter(isInvalidlyTyped).collect(Collectors.toList());

        if (!invalidlyTypedAttributes.isEmpty()) {
            final String exceptionMessage = invalidlyTypedAttributes.stream().map(this::makeInvalidlyTypedAttributeMessage)
                    .collect(Collectors.joining("\n "));
            throw new RequestVerificationException(exceptionMessage);
        }
    }

    private String makeInvalidlyTypedAttributeMessage(Attribute attribute) {
        return String.format("%s must be of type %s. %s", attribute.getAttributeName(),
                attribute.getDataType().toString(), attribute.getDataType().getAdditionalErrorMessage());
    }
}
