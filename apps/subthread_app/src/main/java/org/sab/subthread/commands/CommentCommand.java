package org.sab.subthread.commands;

import org.json.JSONObject;
import org.sab.models.CollectionNames;
import org.sab.models.CommentAttributes;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.rabbitmq.RPCClient;
import org.sab.service.validation.CommandWithVerification;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public abstract class CommentCommand extends CommandWithVerification {

    // TODO rename |PARENT_SUBTHREAD_ID| to |PARENT_CONTENT_ID|.
    protected static final String PARENT_SUBTHREAD_ID = CommentAttributes.PARENT_SUBTHREAD_ID.getHTTP();
    protected static final String CREATOR_ID = CommentAttributes.CREATOR_ID.getHTTP();
    protected static final String LIKES = CommentAttributes.LIKES.getHTTP();
    protected static final String DISLIKES = CommentAttributes.DISLIKES.getHTTP();
    protected static final String CONTENT = CommentAttributes.CONTENT.getHTTP();
    //    protected static final String DATE_CREATED = "dateCreated";
    protected static final String COMMENT_ID = CommentAttributes.COMMENT_ID.getHTTP();
    protected static final String PARENT_CONTENT_TYPE = CommentAttributes.PARENT_CONTENT_TYPE.getHTTP();

    protected static final String PARENT_SUBTHREAD_ID_DB = CommentAttributes.PARENT_SUBTHREAD_ID.getDb();
    protected static final String CREATOR_ID_DB = CommentAttributes.CREATOR_ID.getDb();
    protected static final String LIKES_DB = CommentAttributes.LIKES.getDb();
    protected static final String DISLIKES_DB = CommentAttributes.DISLIKES.getDb();
    protected static final String CONTENT_DB = CommentAttributes.CONTENT.getDb();
    protected static final String DATE_CREATED_DB = CommentAttributes.DATE_CREATED.getDb();
    protected static final String COMMENT_ID_DB = CommentAttributes.COMMENT_ID.getDb();
    protected static final String PARENT_CONTENT_TYPE_DB = CommentAttributes.PARENT_CONTENT_TYPE.getDb();

    protected static final String USER_ID = UserAttributes.USER_ID.getHTTP();
    protected static final String ACTION_MAKER_ID = "userId";

    protected static final String OBJECT_NOT_FOUND = "The data you are requested does not exist.";
    protected static final String REQUESTER_NOT_AUTHOR = "You are not the author of this comment";

    // Subthread attributes
    // http
    protected static final String SUBTHREAD_ID = SubThreadAttributes.SUBTHREAD_ID.getHTTP();
    protected static final String SUBTHREAD_PARENT_THREAD_ID = SubThreadAttributes.PARENT_THREAD_ID.getHTTP();
    protected static final String SUBTHREAD_CREATOR_ID = SubThreadAttributes.CREATOR_ID.getHTTP();

    protected static final String SUBTHREAD_TITLE = SubThreadAttributes.TITLE.getHTTP();
    protected static final String SUBTHREAD_CONTENT = SubThreadAttributes.CONTENT.getHTTP();

    protected static final String SUBTHREAD_LIKES = SubThreadAttributes.LIKES.getHTTP();
    protected static final String SUBTHREAD_DISLIKES = SubThreadAttributes.DISLIKES.getHTTP();

    protected static final String SUBTHREAD_HASIMAGE = SubThreadAttributes.HAS_IMAGE.getHTTP();

    // Subthread attributes
    // db
    protected static final String SUBTHREAD_SUBTHREAD_ID_DB = SubThreadAttributes.SUBTHREAD_ID.getDb();
    protected static final String SUBTHREAD_PARENT_THREAD_ID_DB = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    protected static final String SUBTHREAD_CREATOR_ID_DB = SubThreadAttributes.CREATOR_ID.getDb();

    protected static final String SUBTHREAD_TITLE_DB = SubThreadAttributes.TITLE.getDb();
    protected static final String SUBTHREAD_CONTENT_DB = SubThreadAttributes.CONTENT.getDb();

    protected static final String SUBTHREAD_LIKES_DB = SubThreadAttributes.LIKES.getDb();
    protected static final String SUBTHREAD_DISLIKES_DB = SubThreadAttributes.DISLIKES.getDb();

    protected static final String SUBTHREAD_HASIMAGE_DB = SubThreadAttributes.HAS_IMAGE.getDb();
    protected static final String SUBTHREAD_DATE_CREATED_DB = SubThreadAttributes.DATE_CREATED.getDb();


    // User attributes
    protected static final String USERNAME = UserAttributes.USERNAME.toString();
    protected static final String USER_ACTION_MAKER_ID = UserAttributes.ACTION_MAKER_ID.getHTTP();
    protected static final String USER_IS_DELETED = UserAttributes.IS_DELETED.getHTTP();
    protected static final String USER_USER_ID = UserAttributes.USER_ID.getHTTP();
    protected static final String USER_NUM_OF_FOLLOWERS = UserAttributes.NUM_OF_FOLLOWERS.getHTTP();

    protected static final String USER_ACTION_MAKER_ID_DB = UserAttributes.ACTION_MAKER_ID.getArangoDb();
    protected static final String USER_IS_DELETED_DB = UserAttributes.IS_DELETED.getArangoDb();
    protected static final String USER_USER_ID_DB = UserAttributes.USER_ID.getArangoDb();
    protected static final String USER_NUM_OF_FOLLOWERS_DB = UserAttributes.NUM_OF_FOLLOWERS.getArangoDb();

    // Thread attributes
    protected static final String THREAD_NAME = ThreadAttributes.THREAD_NAME.getHTTP();
    protected static final String THREAD_DESCRIPTION = ThreadAttributes.DESCRIPTION.getHTTP();
    protected static final String THREAD_CREATOR_ID = ThreadAttributes.CREATOR_ID.getHTTP();
    protected static final String THREAD_NUM_OF_FOLLOWERS = ThreadAttributes.NUM_OF_FOLLOWERS.getHTTP();
    // TODO remove attribute
    protected static final String THREAD_DATE_CREATED = ThreadAttributes.DATE_CREATED.getHTTP();

    protected static final String THREAD_ASSIGNER_ID = ThreadAttributes.ASSIGNER_ID.getHTTP();
    protected static final String THREAD_MODERATOR_ID = ThreadAttributes.MODERATOR_ID.getHTTP();
    protected static final String THREAD_ACTION_MAKER_ID = ThreadAttributes.ACTION_MAKER_ID.getHTTP();
    protected static final String THREAD_BANNED_USER_ID = ThreadAttributes.BANNED_USER_ID.getHTTP();

    protected static final String THREAD_DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String THREAD_CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String THREAD_NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String THREAD_DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();
    // TODO add attribs from enums

    // TODO get from env vars
    protected static final String DB_Name = System.getenv("ARANGO_DB");
    protected static final String TEST_DB_Name = DB_Name;
    // TODO bad name
    protected static final String COMMENT_COLLECTION_NAME = CollectionNames.COMMENT.get();
    protected static final String CONTENT_COMMENT_COLLECTION_NAME = CollectionNames.CONTENT_COMMENT.get();
    protected static final String USER_COLLECTION_NAME = CollectionNames.USER.get();
    protected static final String USER_CREATE_COMMENT_COLLECTION_NAME = CollectionNames.USER_CREATE_COMMENT.get();
    protected static final String USER_LIKE_COMMENT_COLLECTION_NAME = CollectionNames.USER_LIKE_COMMENT.get();
    protected static final String USER_DISLIKE_COMMENT_COLLECTION_NAME = CollectionNames.USER_DISLIKE_COMMENT.get();
    protected static final String SUBTHREAD_COLLECTION_NAME = CollectionNames.SUBTHREAD.get();
    protected static final String THREAD_COLLECTION_NAME = CollectionNames.THREAD.get();


    // TODO get queueName from somewhere instead of hardcoding it
    protected static final String Notification_Queue_Name = "NOTIFICATION_REQ";

    public static void tag(String content, String contentId){
        String[] words = content.split(" ");
        for(String word:words){
            if(word.startsWith("@")){
                String user = word.substring(1);
                JSONObject request = new JSONObject();
//                request.put("userId", user);
//                request.put("contentId", contentId);
                try (RPCClient rpcClient = RPCClient.getInstance()) {
                    rpcClient.call_withoutResponse(request.toString(), Notification_Queue_Name);
                }
                catch (IOException | TimeoutException | InterruptedException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
