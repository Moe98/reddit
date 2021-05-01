package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.functions.TypeUtilities;
import org.sab.service.Command;

import java.util.Set;

public abstract class UserCommand extends Command {
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

    protected abstract String verifyBody();

    //instance methods
    protected boolean isInBody(String attribute) {
        return body.keySet().contains(attribute);
    }

    protected String verifyBody(String[] params, TypeUtilities.Type[] types, boolean[] isRequired) {
        Set<String> keySet = body.keySet();

        String missing = null;
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            boolean contains = keySet.contains(param);
            if (!contains && isRequired[i]) {
                if (missing == null)
                    missing = "";
                else
                    missing += ", ";

                missing += param;

            }
            if (contains && !TypeUtilities.isType(body.get(param), types[i]))
                return String.format("%s must be of type %s", param, types[i]);
        }
        return missing == null ? null : String.format("You must insert %s in the request body", missing);

    }

    //static methods
    protected static JSONObject sendError(String msg, int statusCode) {
        JSONObject error = new JSONObject().put("msg", msg).put("statusCode", statusCode);
        return error;
    }

    protected static JSONObject sendData(JSONObject data) {
        JSONObject response = new JSONObject().put("data", data);
        response.put("statusCode", 200);
        return response;
    }


}
