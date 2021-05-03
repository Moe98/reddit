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

    protected JSONObject body, uriParams;
    protected Schema schema;

    protected String getFromUriParams(String attribute) {
        return (String) uriParams.getJSONArray(attribute).get(0);
    }

    @Override
    public final String execute(JSONObject request) {

        schema = getSchema();
        uriParams = request.getJSONObject("uriParams");
        if (request.getString("methodType").equals("GET")) {
            if (!schema.isEmpty())
                return Responder.makeErrorResponse(String.format("%s expects a body. Don't use a GET Request!", getClass().getSimpleName()), 500).toString();
            body = new JSONObject();
        } else
            body = request.getJSONObject("body");
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
        return String.format("%s must be of type %s.%s", attribute.getAttributeName(),
                attribute.getDataType().toString(), attribute.getDataType().getAdditionalErrorMessage());
    }
}
