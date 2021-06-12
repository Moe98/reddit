package org.sab.innerAppComm;

import org.json.JSONObject;
import org.sab.models.NotificationAttributes;
import org.sab.models.RequestAttributes;
import org.sab.rabbitmq.RPCClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Comm {
    // TODO get queueName from somewhere instead of hardcoding it
    protected static final String Notification_Queue_Name = "NOTIFICATION_REQ";

    public static void tag(String title, String contentId, String content){
        String[] words = content.split(" ");
        ArrayList<String> usersList = new ArrayList<String>();
        for(String word:words){
            if(word.startsWith("@")){
                usersList.add(word.substring(1));
            }
        }
        putMessageInNotificationQueue(title, (String[]) usersList.toArray(), contentId);
    }

    public static void putMessageInNotificationQueue(String title, String[] usersList, String notificationBody){
        JSONObject body = new JSONObject();
        body.put(NotificationAttributes.TITLE.getHTTP(), title);
        body.put(NotificationAttributes.USERS_LIST.getHTTP(), usersList);
        body.put(NotificationAttributes.NOTIFICATION_BODY.getHTTP(), notificationBody);

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put(RequestAttributes.BODY.getHTTP(), body);
        request.put(RequestAttributes.METHOD_TYPE.getHTTP(), RequestType.PUT);
        request.put(RequestAttributes.URI_PARAMS.getHTTP(), uriParams);


        try (RPCClient rpcClient = RPCClient.getInstance()) {
            rpcClient.call_withoutResponse(request.toString(), Notification_Queue_Name);
            System.out.println("finished");
        }
        catch (IOException | TimeoutException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String[] arr = new String[0];
        putMessageInNotificationQueue("hohoh",arr, "comment\423132");
        System.out.println("dodododd");
    }
}
