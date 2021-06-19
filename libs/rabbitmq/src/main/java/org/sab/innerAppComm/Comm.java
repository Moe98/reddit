package org.sab.innerAppComm;

import org.json.JSONArray;
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

    public static void tag(String queueName, String title, String contentId, String content, String functionName){
        if(content!=null && content.length()>0){
            String[] words = content.split(" ");
            ArrayList<String> usersList = new ArrayList<String>();
            for(String word:words){
                if(word.startsWith("@")){
                    usersList.add(word.substring(1));
                }
            }
            if(usersList.size()>0){
                JSONObject body = new JSONObject();
                body.put(NotificationAttributes.TITLE.getValue(), title);
                body.put(NotificationAttributes.USERS_LIST.getValue(), usersList);
                body.put(NotificationAttributes.NOTIFICATION_BODY.getValue(), contentId);

                JSONObject uriParams = new JSONObject();

                JSONObject authenticationParams = new JSONObject();
                authenticationParams.put(AuthenticationAttributes.IS_AUTHENTICATED.getValue(), true);

                JSONObject request = new JSONObject();
                request.put(RequestAttributes.FUNCTION_NAME.getValue(), functionName);
                request.put(RequestAttributes.BODY.getValue(), body);
                request.put(RequestAttributes.METHOD_TYPE.getValue(), RequestType.PUT);
                request.put(RequestAttributes.URI_PARAMS.getValue(), uriParams);
                request.put(RequestAttributes.AUTHENTICATION_PARAMS.getValue(), authenticationParams);

                putMessageInQueue(request, queueName);
            }
        }
    }

    public static void notifyApp(String queueName, String title, String contentId, String userName, String functionName){
        String[] usersList = new String[1];
        usersList[0] = userName;
        JSONObject body = new JSONObject();
        body.put(NotificationAttributes.TITLE.getValue(), title);
        body.put(NotificationAttributes.USERS_LIST.getValue(), usersList);
        body.put(NotificationAttributes.NOTIFICATION_BODY.getValue(), contentId);

        JSONObject uriParams = new JSONObject();

        JSONObject authenticationParams = new JSONObject();
        authenticationParams.put(AuthenticationAttributes.IS_AUTHENTICATED.getValue(), true);

        JSONObject request = new JSONObject();
        request.put(RequestAttributes.FUNCTION_NAME.getValue(), functionName);
        request.put(RequestAttributes.BODY.getValue(), body);
        request.put(RequestAttributes.METHOD_TYPE.getValue(), RequestType.PUT);
        request.put(RequestAttributes.URI_PARAMS.getValue(), uriParams);
        request.put(RequestAttributes.AUTHENTICATION_PARAMS.getValue(), authenticationParams);

        putMessageInQueue(request, queueName);
    }

    public static void notifyApp(String queueName, String title, String contentId, ArrayList<String> userNames, String functionName){
        if(userNames.size()>0){
            String[] userList = new String[userNames.size()];
            for(int i=0;i<userNames.size();i++) {
                userList[i] = userNames.get(i);
            }
            JSONObject body = new JSONObject();
            body.put(NotificationAttributes.TITLE.getValue(), title);
            body.put(NotificationAttributes.USERS_LIST.getValue(),userList);
            body.put(NotificationAttributes.NOTIFICATION_BODY.getValue(), contentId);

            JSONObject uriParams = new JSONObject();

            JSONObject authenticationParams = new JSONObject();
            authenticationParams.put(AuthenticationAttributes.IS_AUTHENTICATED.getValue(), true);

            JSONObject request = new JSONObject();
            request.put(RequestAttributes.FUNCTION_NAME.getValue(), functionName);
            request.put(RequestAttributes.BODY.getValue(), body);
            request.put(RequestAttributes.METHOD_TYPE.getValue(), RequestType.PUT);
            request.put(RequestAttributes.URI_PARAMS.getValue(), uriParams);
            request.put(RequestAttributes.AUTHENTICATION_PARAMS.getValue(), authenticationParams);

            putMessageInQueue(request, queueName);
        }

    }

    public static void updateRecommendation(String queueName, String userName, String functionName){
        JSONObject body = new JSONObject();
        body.put(UserAttributes.USERNAME.getHTTP(), userName);

        JSONObject uriParams = new JSONObject();

        JSONObject authenticationParams = new JSONObject();
        authenticationParams.put(AuthenticationAttributes.IS_AUTHENTICATED.getValue(), true);

        JSONObject request = new JSONObject();
        request.put(RequestAttributes.FUNCTION_NAME.getValue(), functionName);
        request.put(RequestAttributes.BODY.getValue(), body);
        request.put(RequestAttributes.METHOD_TYPE.getValue(), RequestType.POST);
        request.put(RequestAttributes.URI_PARAMS.getValue(), uriParams);
        request.put(RequestAttributes.AUTHENTICATION_PARAMS.getValue(), authenticationParams);

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
