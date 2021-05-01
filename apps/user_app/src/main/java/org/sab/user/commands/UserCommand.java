package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.validation.Attribute;
import org.sab.validation.Schema;
import org.sab.validation.TypeUtilities;
import org.sab.service.Command;


public abstract class UserCommand extends Command {
    protected static final String USERNAME = "username";
    protected static final String EMAIL = "email";
    protected static final String PASSWORD = "password";
    protected static final String BIRTHDATE = "birthdate";
    protected static final String PHOTO_URL = "photoUrl";


    protected JSONObject body;

    @Override
    public String execute(JSONObject request) {
        body = request.getJSONObject("body");
        String verifyBody = verifyBody();
        if (verifyBody != null)
            return sendError(verifyBody, 400).toString();
        return execute();

    }

    //abstract methods
    protected abstract String execute();

    protected abstract Schema getSchema();

    //instance methods
    protected boolean isInBody(String attribute) {
        return body.keySet().contains(attribute);
    }

    protected String verifyBody(Schema schema) {

        String missing = null;
        for (Attribute attribute : schema.getAttributeList()) {
            String param = attribute.getAttributeName();
            boolean contains = isInBody(param);
            if (!contains && attribute.isRequired()) {
                if (missing == null)
                    missing = "";
                else
                    missing += ", ";

                missing += param;

            }
            if (contains) {
                String typeStatus = TypeUtilities.isType(body.get(param), attribute.getDataType());
                if (typeStatus != null)
                    return String.format("%s must be of type %s.%s", param, attribute.getDataType(), typeStatus.isEmpty() ? "" : " " + typeStatus);

            }

        }
        return missing == null ? null : String.format("You must insert %s in the request body", missing);

    }


    protected JSONObject makeErrorResponse(String msg, int statusCode) {
        JSONObject error = new JSONObject().put("msg", msg).put("statusCode", statusCode);
        return error;
    }

    protected JSONObject makeDataResponse(JSONObject data) {
        JSONObject response = new JSONObject().put("data", data);
        response.put("statusCode", 200);
        return response;
    }
}
