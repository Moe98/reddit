package org.sab.innerAppComm;

import org.json.JSONObject;
import org.sab.models.AuthenticationAttributes;
import org.sab.models.NotificationAttributes;
import org.sab.models.RequestAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.rabbitmq.RPCClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Comm {
    // TODO get queueName from somewhere instead of hardcoding it
    protected static final String Notification_Queue_Name = "NOTIFICATION_REQ";

    public static void tag(String queueName, String title, String contentId, String content){
        String[] words = content.split(" ");
        ArrayList<String> usersList = new ArrayList<String>();
        for(String word:words){
            if(word.startsWith("@")){
                usersList.add(word.substring(1));
            }
        }
        JSONObject body = new JSONObject();
        body.put(NotificationAttributes.TITLE.getHTTP(), title);
        body.put(NotificationAttributes.USERS_LIST.getHTTP(), usersList);
        body.put(NotificationAttributes.NOTIFICATION_BODY.getHTTP(), contentId);

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put(RequestAttributes.BODY.getHTTP(), body);
        request.put(RequestAttributes.METHOD_TYPE.getHTTP(), RequestType.PUT);
        request.put(RequestAttributes.URI_PARAMS.getHTTP(), uriParams);

        putMessageInQueue(request, queueName);
    }

    public static void updateRecommendation(String queueName, String userName){
        JSONObject body = new JSONObject();
        body.put(UserAttributes.USERNAME.getHTTP(), userName);

        JSONObject uriParams = new JSONObject();

        JSONObject authenticationParams = new JSONObject();
        authenticationParams.put(AuthenticationAttributes.IS_AUTHENTICATED.getHTTP(), true);

        JSONObject request = new JSONObject();
        request.put("functionName", "UPDATE_RECOMMENDED_USERS");
        request.put(RequestAttributes.BODY.getHTTP(), body);
        request.put(RequestAttributes.METHOD_TYPE.getHTTP(), RequestType.POST);
        request.put(RequestAttributes.URI_PARAMS.getHTTP(), uriParams);
        request.put(RequestAttributes.AUTHENTICATION_PARAMS.getHTTP(), authenticationParams);

        putMessageInQueue(request, queueName);
    }

    public static void putMessageInQueue(JSONObject request, String queueName){
        try (RPCClient rpcClient = RPCClient.getInstance()) {
            rpcClient.call_withoutResponse(request.toString(), queueName);
        }
        catch (IOException | TimeoutException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
