package org.sab.arango;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.sab.models.CollectionNames;
import org.sab.models.CommentAttributes;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.models.report.SubThreadReportAttributes;
import org.sab.models.user.UserAttributes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PopulateArango {

    private static final String dbName = System.getenv("ARANGO_DB");

    private static final String threadDescription = ThreadAttributes.DESCRIPTION.getDb();
    private static final String threadCreator = ThreadAttributes.CREATOR_ID.getDb();
    private static final String threadFollowers = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    private static final String threadDate = ThreadAttributes.DATE_CREATED.getDb();

    private static final String subThreadParentThread = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    private static final String subThreadTitle = SubThreadAttributes.TITLE.getDb();
    private static final String subThreadCreator = SubThreadAttributes.CREATOR_ID.getDb();
    private static final String subThreadLikes = SubThreadAttributes.LIKES.getDb();
    private static final String subThreadDislikes = SubThreadAttributes.DISLIKES.getDb();
    private static final String subThreadContent = SubThreadAttributes.CONTENT.getDb();
    private static final String subThreadHasImage = SubThreadAttributes.HAS_IMAGE.getDb();
    private static final String subThreadDate = SubThreadAttributes.DATE_CREATED.getDb();

    private static final String commentCreatorID = CommentAttributes.CREATOR_ID.getDb();
    private static final String commentContent = CommentAttributes.CONTENT.getDb();
    private static final String commentLikes = CommentAttributes.LIKES.getDb();
    private static final String commentDislikes = CommentAttributes.DISLIKES.getDb();
    private static final String commentDate = CommentAttributes.DISLIKES.getDb();
    private static final String commentParentId = CommentAttributes.PARENT_SUBTHREAD_ID.getDb();
    private static final String commentParentType = CommentAttributes.PARENT_CONTENT_TYPE.getDb();

    private static final String reportSubThreadId = SubThreadReportAttributes.SUBTHREAD_Id.getDb();
    private static final String reportParentId = SubThreadReportAttributes.PARENT_THREAD_ID.getDb();
    private static final String reportUserName = SubThreadReportAttributes.REPORTER_ID.getDb();
    private static final String reportType = SubThreadReportAttributes.TYPE_OF_REPORT.getDb();
    private static final String reportMessage = SubThreadReportAttributes.REPORT_MSG.getDb();
    private static final String reportDate = SubThreadReportAttributes.DATE_CREATED.getDb();

    private static final String userIsDeleted = UserAttributes.IS_DELETED.getArangoDb();
    private static final String userNumOfFollowers = UserAttributes.NUM_OF_FOLLOWERS.getArangoDb();

    private static final String USERS_COLLECTION_NAME = CollectionNames.USER.get();

    private static final String THREADS_COLLECTION_NAME = CollectionNames.THREAD.get();
    private static final String SUB_THREADS_COLLECTION_NAME = CollectionNames.SUBTHREAD.get();
    private static final String REPORTS_COLLECTION_NAME = CollectionNames.SUBTHREAD_REPORTS.get();
    private static final String COMMENT_COLLECTION_NAME = CollectionNames.COMMENT.get();

    private static final String THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME = CollectionNames.THREAD_CONTAIN_SUBTHREAD.get();
    private static final String USER_FOLLOW_USER_COLLECTION_NAME = CollectionNames.USER_FOLLOW_USER.get();
    private static final String USER_BLOCK_USER_COLLECTION_NAME = CollectionNames.USER_BLOCK_USER.get();
    private static final String USER_FOLLOW_THREAD_COLLECTION_NAME = CollectionNames.USER_FOLLOW_THREAD.get();
    private static final String USER_MOD_THREAD_COLLECTION_NAME = CollectionNames.USER_MOD_THREAD.get();
    private static final String USER_CREATE_COMMENT_COLLECTION_NAME = CollectionNames.USER_CREATE_COMMENT.get();
    private static final String CONTENT_COMMENT_COLLECTION_NAME = CollectionNames.CONTENT_COMMENT.get();

    // TODO from thread
    private static final String USER_BOOKMARK_THREAD_COLLECTION_NAME = CollectionNames.USER_BOOKMARK_THREAD.get();
    private static final String USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME = CollectionNames.USER_BOOKMARK_SUBTHREAD.get();
    private static final String USER_LIKE_SUBTHREAD_COLLECTION_NAME = CollectionNames.USER_LIKE_SUBTHREAD.get();
    private static final String USER_DISLIKE_SUBTHREAD_COLLECTION_NAME = CollectionNames.USER_DISLIKE_SUBTHREAD.get();
    private static final String USER_LIKE_COMMENT_COLLECTION_NAME = CollectionNames.USER_LIKE_COMMENT.get();
    private static final String USER_DISLIKE_COMMENT_COLLECTION_NAME = CollectionNames.USER_DISLIKE_COMMENT.get();

    private static final String USER_BANNED_FROM_THREAD_COLLECTION_NAME = CollectionNames.USER_BANNED_FROM_THREAD.get();

    public static void populate(Arango arango) {
        arango.createDatabaseIfNotExists(dbName);
        arango.createCollectionIfNotExists(dbName, THREADS_COLLECTION_NAME, false);
        arango.createCollectionIfNotExists(dbName, SUB_THREADS_COLLECTION_NAME, false);
        arango.createCollectionIfNotExists(dbName, USERS_COLLECTION_NAME, false);
        arango.createCollectionIfNotExists(dbName, COMMENT_COLLECTION_NAME, false);
        arango.createCollectionIfNotExists(dbName, REPORTS_COLLECTION_NAME, false);

        arango.createCollectionIfNotExists(dbName, THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_FOLLOW_USER_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_BLOCK_USER_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_FOLLOW_THREAD_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_MOD_THREAD_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, CONTENT_COMMENT_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_CREATE_COMMENT_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_BOOKMARK_THREAD_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_LIKE_SUBTHREAD_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_LIKE_COMMENT_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_DISLIKE_COMMENT_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(dbName, USER_BANNED_FROM_THREAD_COLLECTION_NAME, true);

        String[] subThreads;
        String[] threads;
        String[] users;
        String[] comments;
        String[] reports;

        HashMap<String, String> threadCreatorMap = new HashMap<>();
        HashMap<String, String> subThreadParentMap = new HashMap<>();

        HashMap<String, Integer> threadNumFollowMap = new HashMap<>();
        HashMap<String, Integer> subThreadNumLikeMap = new HashMap<>();
        HashMap<String, Integer> subThreadNumDislikeMap = new HashMap<>();
        HashMap<String, Integer> commentNumLikeMap = new HashMap<>();
        HashMap<String, Integer> commentNumDislikeMap = new HashMap<>();

        int userNum = 100, threadNum = 200, subthreadNum = 200, commentNum = 200, reportsNum = 20;
        int usersToFollowNum = 5, threadsToFollowNum = 5, threadsToBookmarkNum = 5, numToBan = 50;
        int usersToBlockNum = 5, subThreadsToBookmarkNum = 5, subThreadsToLikeNum = 5, subThreadsToDislikeNum = 5;
        int commentsToLikeNum = 5, commentsToDislikeNum = 5;

//        userNum++;

        // Dummy Data
        users = new String[userNum];
        threads = new String[threadNum];
        subThreads = new String[subthreadNum];
        comments = new String[commentNum];
        reports = new String[reportsNum];

        for (int i = 0; i < userNum; i++) {
            BaseDocument user = new BaseDocument();
            // TODO add the rest of the attributes
            String userName = "users" + i;
            user.setKey(userName);
            user.addAttribute(userIsDeleted, false);
            user.addAttribute(userNumOfFollowers, 0);
            arango.createDocument(dbName, USERS_COLLECTION_NAME, user);
            users[i] = userName;
        }
//        BaseDocument user = new BaseDocument();
//        String userName = "Ronic";
//        user.setKey(userName);
//        user.addAttribute(userIsDeleted, false);
//        user.addAttribute(userNumOfFollowers, 0);
//        arango.createDocument(dbName, USERS_COLLECTION_NAME, user);
//        users[userNum-1] = userName;
        System.out.println("Done with " + USERS_COLLECTION_NAME);

        BaseEdgeDocument edgeDocument;
        for (int i = 0; i < threadNum; i++) {
            String threadName = "thread" + i;
            String creator = users[i % users.length];

            BaseDocument thread = new BaseDocument();
            thread.setKey(threadName);
            thread.addAttribute(threadDescription, "thread description" + i);
            thread.addAttribute(threadCreator, creator);
            thread.addAttribute(threadFollowers, 0);
            thread.addAttribute(threadDate, Timestamp.valueOf(LocalDateTime.now()));
            arango.createDocument(dbName, THREADS_COLLECTION_NAME, thread);
            threads[i] = threadName;

            edgeDocument = new BaseEdgeDocument();
            edgeDocument.setFrom(USERS_COLLECTION_NAME + "/" + creator);
            edgeDocument.setTo(THREADS_COLLECTION_NAME + "/" + threadName);

            threadCreatorMap.put(threadName, creator);

            arango.createEdgeDocument(dbName, USER_MOD_THREAD_COLLECTION_NAME, edgeDocument);
        }
        System.out.println("Done with " + THREADS_COLLECTION_NAME);

        for (int i = 0; i < subthreadNum; i++) {
            BaseDocument subThread = new BaseDocument();
            String threadName = threads[i % threads.length];
            subThread.addAttribute(subThreadParentThread, threadName);
            subThread.addAttribute(subThreadTitle, "title" + i);
            subThread.addAttribute(subThreadCreator, users[i % users.length]);
            subThread.addAttribute(subThreadLikes, 0);
            subThread.addAttribute(subThreadDislikes, 0);
            subThread.addAttribute(subThreadContent, "content");
            subThread.addAttribute(subThreadHasImage, false);
            subThread.addAttribute(subThreadDate, Timestamp.valueOf(LocalDateTime.now()));
            BaseDocument created1 = arango.createDocument(dbName, SUB_THREADS_COLLECTION_NAME, subThread);
            subThreads[i] = created1.getKey();

            edgeDocument = new BaseEdgeDocument();
            edgeDocument.setTo(SUB_THREADS_COLLECTION_NAME + "/" + subThreads[i]);
            edgeDocument.setFrom(THREADS_COLLECTION_NAME + "/" + threadName);

            arango.createEdgeDocument(dbName, THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME, edgeDocument);
            subThreadParentMap.put(subThreads[i], threadName);
        }
        System.out.println("Done with " + SUB_THREADS_COLLECTION_NAME + " and " + THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME);

        for (int i = 0; i < reportsNum; i++) {
            BaseDocument report = new BaseDocument();
            String subThreadId = subThreads[i % subThreads.length];
            String parentThreadId = subThreadParentMap.get(subThreadId);

            String typeOfReport = "SUBTHREAD_REPORT";
            String message = "This is a report";
            report.addAttribute(reportSubThreadId, subThreadId);
            report.addAttribute(reportParentId, parentThreadId);
            report.addAttribute(reportUserName, users[(i + 100) % users.length]);
            report.addAttribute(reportType, typeOfReport);
            report.addAttribute(reportMessage, message);
            report.addAttribute(reportDate, Timestamp.valueOf(LocalDateTime.now()));
            BaseDocument created1 = arango.createDocument(dbName, REPORTS_COLLECTION_NAME, report);
            reports[i] = created1.getKey();
        }
        System.out.println("Done with " + REPORTS_COLLECTION_NAME);

        for (int i = 0; i < commentNum; i++) {

            BaseDocument comment = new BaseDocument();
            String subthreadName = subThreads[i];
            String creatorId = users[i % users.length];

            comment.addAttribute(commentParentId, subthreadName);
            comment.addAttribute(commentCreatorID, creatorId);
            comment.addAttribute(commentContent, "This is a comment");
            comment.addAttribute(commentParentType, "subthread");
            comment.addAttribute(commentLikes, 0);
            comment.addAttribute(commentDislikes, 0);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            comment.addAttribute(commentDate, sqlDate);

            BaseDocument created1 = arango.createDocument(dbName, COMMENT_COLLECTION_NAME, comment);
            comments[i] = created1.getKey();

            edgeDocument = new BaseEdgeDocument();
            edgeDocument.setTo(USERS_COLLECTION_NAME + "/" + creatorId);
            edgeDocument.setFrom(COMMENT_COLLECTION_NAME + "/" + comments[i]);
            arango.createEdgeDocument(dbName, USER_CREATE_COMMENT_COLLECTION_NAME, edgeDocument);

            edgeDocument = new BaseEdgeDocument();
            edgeDocument.setTo(SUB_THREADS_COLLECTION_NAME + "/" + subthreadName);
            edgeDocument.setFrom(COMMENT_COLLECTION_NAME + "/" + comments[i]);
            arango.createEdgeDocument(dbName, CONTENT_COMMENT_COLLECTION_NAME, edgeDocument);
        }

        System.out.println("Done with " + COMMENT_COLLECTION_NAME);


        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = threadNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(range);

            while (j < threadsToFollowNum) {
                edgeDocument = new BaseEdgeDocument();

                String threadName = threads[range.get(j)];
                String from = USERS_COLLECTION_NAME + "/" + users[i];
                String to = THREADS_COLLECTION_NAME + "/" + threadName;
                edgeDocument.setFrom(from);
                edgeDocument.setTo(to);

                arango.createEdgeDocument(dbName, USER_FOLLOW_THREAD_COLLECTION_NAME, edgeDocument);
                threadNumFollowMap.put(threadName, threadNumFollowMap.getOrDefault(threadName, 0) + 1);
                j++;
            }
        }
        System.out.println("Done with " + USER_FOLLOW_THREAD_COLLECTION_NAME);

        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = userNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            range.remove(i);
            Collections.shuffle(range);

            while (j < usersToFollowNum) {
                edgeDocument = new BaseEdgeDocument();


                edgeDocument.setFrom(USERS_COLLECTION_NAME + "/" + users[i]);
                edgeDocument.setTo(USERS_COLLECTION_NAME + "/" + users[range.get(j)]);
                arango.createEdgeDocument(dbName, USER_FOLLOW_USER_COLLECTION_NAME, edgeDocument);
                j++;
            }
        }
        System.out.println("Done with " + USER_FOLLOW_USER_COLLECTION_NAME);

        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = userNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            range.remove(i);
            Collections.shuffle(range);

            while (j < usersToBlockNum) {
                edgeDocument = new BaseEdgeDocument();

                edgeDocument.setFrom(USERS_COLLECTION_NAME + "/" + users[i]);
                edgeDocument.setTo(USERS_COLLECTION_NAME + "/" + users[range.get(j)]);
                arango.createEdgeDocument(dbName, USER_BLOCK_USER_COLLECTION_NAME, edgeDocument);
                j++;
            }
        }
        System.out.println("Done with " + USER_BLOCK_USER_COLLECTION_NAME);

        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = threadNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(range);

            while (j < threadsToBookmarkNum) {
                edgeDocument = new BaseEdgeDocument();

                String from = USERS_COLLECTION_NAME + "/" + users[i];
                String to = THREADS_COLLECTION_NAME + "/" + threads[range.get(j)];
                edgeDocument.setFrom(from);
                edgeDocument.setTo(to);

                arango.createEdgeDocument(dbName, USER_BOOKMARK_THREAD_COLLECTION_NAME, edgeDocument);
                j++;
            }
        }
        System.out.println("Done with " + USER_BOOKMARK_THREAD_COLLECTION_NAME);

        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = threadNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(range);

            while (j < subThreadsToBookmarkNum) {
                edgeDocument = new BaseEdgeDocument();

                String from = USERS_COLLECTION_NAME + "/" + users[i];
                String to = SUB_THREADS_COLLECTION_NAME + "/" + threads[range.get(j)];
                edgeDocument.setFrom(from);
                edgeDocument.setTo(to);

                arango.createEdgeDocument(dbName, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, edgeDocument);
                j++;
            }
        }
        System.out.println("Done with " + USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME);

        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = subthreadNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(range);

            while (j < subThreadsToLikeNum) {
                edgeDocument = new BaseEdgeDocument();

                String subthreadName = subThreads[range.get(j)];
                String from = USERS_COLLECTION_NAME + "/" + users[i];
                String to = SUB_THREADS_COLLECTION_NAME + "/" + subthreadName;
                edgeDocument.setFrom(from);
                edgeDocument.setTo(to);

                arango.createEdgeDocument(dbName, USER_LIKE_SUBTHREAD_COLLECTION_NAME, edgeDocument);
                subThreadNumLikeMap.put(subthreadName, subThreadNumLikeMap.getOrDefault(subthreadName, 0) + 1);
                j++;
            }
        }
        System.out.println("Done with " + USER_LIKE_SUBTHREAD_COLLECTION_NAME);

        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = subthreadNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(range);

            while (j < subThreadsToDislikeNum) {
                edgeDocument = new BaseEdgeDocument();

                String subthreadName = subThreads[range.get(j)];
                String from = USERS_COLLECTION_NAME + "/" + users[i];
                String to = SUB_THREADS_COLLECTION_NAME + "/" + subthreadName;
                edgeDocument.setFrom(from);
                edgeDocument.setTo(to);

                arango.createEdgeDocument(dbName, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, edgeDocument);
                subThreadNumDislikeMap.put(subthreadName, subThreadNumDislikeMap.getOrDefault(subthreadName, 0) + 1);
                j++;
            }
        }
        System.out.println("Done with " + USER_DISLIKE_SUBTHREAD_COLLECTION_NAME);

        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = commentNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(range);

            while (j < commentsToLikeNum) {
                edgeDocument = new BaseEdgeDocument();

                String commentId = comments[range.get(j)];
                String from = USERS_COLLECTION_NAME + "/" + users[i];
                String to = COMMENT_COLLECTION_NAME + "/" + commentId;
                edgeDocument.setFrom(from);
                edgeDocument.setTo(to);

                arango.createEdgeDocument(dbName, USER_LIKE_COMMENT_COLLECTION_NAME, edgeDocument);
                commentNumLikeMap.put(commentId, commentNumLikeMap.getOrDefault(commentId, 0) + 1);
                j++;
            }
        }
        System.out.println("Done with " + USER_LIKE_COMMENT_COLLECTION_NAME);

        for (int i = 0; i < users.length; i++) {
            int j = 0;

            int min = 0, max = commentNum;
            List<Integer> range = IntStream.range(min, max).boxed()
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(range);

            while (j < commentsToDislikeNum) {
                edgeDocument = new BaseEdgeDocument();

                String commentId = comments[range.get(j)];
                String from = USERS_COLLECTION_NAME + "/" + users[i];
                String to = COMMENT_COLLECTION_NAME + "/" + commentId;
                edgeDocument.setFrom(from);
                edgeDocument.setTo(to);

                arango.createEdgeDocument(dbName, USER_DISLIKE_COMMENT_COLLECTION_NAME, edgeDocument);
                commentNumDislikeMap.put(commentId, commentNumDislikeMap.getOrDefault(commentId, 0) + 1);
                j++;
            }
        }
        System.out.println("Done with " + USER_DISLIKE_COMMENT_COLLECTION_NAME);

        int min = 0, max = userNum;
        List<Integer> range = IntStream.range(min, max).boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(range);
        for (int i = 0; i < numToBan; i++) {

            String toBan = users[range.get(i)];
            String thread;
            String mod;

            do {
                thread = threads[(int) (Math.random() * threads.length)];
                mod = threadCreatorMap.get(thread);
            } while (mod.equals(toBan));

            edgeDocument = new BaseEdgeDocument();
//            System.out.println("Ban: " + toBan + "from thread: " + thread + " by mod: " + mod);

            String from = USERS_COLLECTION_NAME + "/" + toBan;
            String to = THREADS_COLLECTION_NAME + "/" + thread;
            edgeDocument.setFrom(from);
            edgeDocument.setTo(to);

            arango.createEdgeDocument(dbName, USER_BANNED_FROM_THREAD_COLLECTION_NAME, edgeDocument);
        }

        System.out.println("Done with " + USER_BANNED_FROM_THREAD_COLLECTION_NAME);

        threadNumFollowMap.forEach((key, value) -> {
            arango.updateDocument(dbName, THREADS_COLLECTION_NAME, Map.of(threadFollowers, value), key);
        });
        subThreadNumLikeMap.forEach((key, value) -> {
            arango.updateDocument(dbName, SUB_THREADS_COLLECTION_NAME, Map.of(subThreadLikes, value), key);
        });
        subThreadNumDislikeMap.forEach((key, value) -> {
            arango.updateDocument(dbName, SUB_THREADS_COLLECTION_NAME, Map.of(subThreadDislikes, value), key);
        });
        commentNumLikeMap.forEach((key, value) -> {
            arango.updateDocument(dbName, COMMENT_COLLECTION_NAME, Map.of(commentLikes, value), key);
        });
        commentNumDislikeMap.forEach((key, value) -> {
            arango.updateDocument(dbName, COMMENT_COLLECTION_NAME, Map.of(commentDislikes, value), key);
        });

    }

    public static void main(String[] args) {
        Arango arango = Arango.getConnectedInstance();
        arango.dropDatabase(dbName);
        populate(arango);
        System.out.println("Totally Done");
        arango = null;
    }

}
