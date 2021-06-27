package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ModeratorSeeReportsTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String parentThreadId1 = "asmakElRayes7amido", title1 = "gelaty azza is better", content1 = "fish is ya3", hasImage1 = "false";
    final private static String parentThreadId2 = "GelatiAzza", title2 = "fish is better", content2 = "fish is better", hasImage2 = "false";
    final private static String subthreadId1 = "20001", subthreadId2 = "20002", subthreadId3 = "20003";
    final private static String reportId1 = "40001", reportId2 = "40002";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine, thread1, thread2;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getConnectedInstance();

            arango.createDatabaseIfNotExists(SubThreadCommand.DB_Name);
            createUsers();
            createThreads();
            assignMod(mantaId, parentThreadId1);
            assignMod(moeId, parentThreadId1);
            assignMod(lujineId, parentThreadId2);
            createSubThread(subthreadId1, parentThreadId1, content1, mantaId, title1, hasImage1);
            createSubThread(subthreadId2, parentThreadId2, content2, moeId, title2, hasImage2);
            insertReports("40001", mantaId, "SCAM", parentThreadId1, "help", subthreadId1);
            insertReports("40002", moeId, "SCAM", parentThreadId1, "help", subthreadId1);
            insertReports("40003", lujineId, "SCAM", parentThreadId1, "help", subthreadId1);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        if (!arango.collectionExists(SubThreadCommand.DB_Name, collectionName)) {
            arango.createCollection(SubThreadCommand.DB_Name, collectionName, false);
        }

        arango.createDocument(SubThreadCommand.DB_Name, collectionName, document);
    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(SubThreadCommand.DB_Name);
    }

    public static void createUsers() {
        moe = new BaseDocument();
        moe.setKey(moeId);
        moe.addAttribute(SubThreadCommand.USER_IS_DELETED_DB, false);
        moe.addAttribute(SubThreadCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(moe, SubThreadCommand.USER_COLLECTION_NAME);

        manta = new BaseDocument();
        manta.setKey(mantaId);
        manta.addAttribute(SubThreadCommand.USER_IS_DELETED_DB, false);
        manta.addAttribute(SubThreadCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(manta, SubThreadCommand.USER_COLLECTION_NAME);

        lujine = new BaseDocument();
        lujine.setKey(lujineId);
        lujine.addAttribute(SubThreadCommand.USER_IS_DELETED_DB, false);
        lujine.addAttribute(SubThreadCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(lujine, SubThreadCommand.USER_COLLECTION_NAME);
    }

    public static void createSubThread(String subThreadId, String parentThreadId, String content, String creatorId, String title, String hasImage) {

        BaseDocument comment = new BaseDocument();
        comment.setKey(subThreadId);
        comment.addAttribute(SubThreadCommand.PARENT_THREAD_ID_DB, parentThreadId);
        comment.addAttribute(SubThreadCommand.CREATOR_ID_DB, creatorId);
        comment.addAttribute(SubThreadCommand.CONTENT_DB, content);
        comment.addAttribute(SubThreadCommand.TITLE_DB, title);
        comment.addAttribute(SubThreadCommand.LIKES_DB, 0);
        comment.addAttribute(SubThreadCommand.DISLIKES_DB, 0);
        comment.addAttribute(SubThreadCommand.HAS_IMAGE_DB, hasImage);
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        comment.addAttribute(SubThreadCommand.DATE_CREATED_DB, sqlDate2);

        addObjectToCollection(comment, SubThreadCommand.SUBTHREAD_COLLECTION_NAME);
    }

    public static void createThreads() {
        thread1 = new BaseDocument();
        thread1.setKey(parentThreadId1);
        thread1.addAttribute(SubThreadCommand.THREAD_CREATOR_ID_DB, mantaId);
        thread1.addAttribute(SubThreadCommand.THREAD_DESCRIPTION_DB, "agmad subreddit fl wogod");
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        thread1.addAttribute(SubThreadCommand.THREAD_DATE_CREATED_DB, sqlDate);
        thread1.addAttribute(SubThreadCommand.THREAD_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread1, SubThreadCommand.THREAD_COLLECTION_NAME);

        thread2 = new BaseDocument();
        thread2.setKey(parentThreadId2);
        thread2.addAttribute(SubThreadCommand.THREAD_CREATOR_ID_DB, moeId);
        thread2.addAttribute(SubThreadCommand.THREAD_DESCRIPTION_DB, "tany agmad subreddit fl wogod");
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        thread2.addAttribute(SubThreadCommand.THREAD_DATE_CREATED_DB, sqlDate2);
        thread2.addAttribute(SubThreadCommand.THREAD_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread2, SubThreadCommand.THREAD_COLLECTION_NAME);
    }

    public static void assignMod(String modId, String threadName) {
        arango.createCollectionIfNotExists(SubThreadCommand.DB_Name, SubThreadCommand.USER_MOD_THREAD_COLLECTION_NAME, true);
        BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
        edgeDocument.setFrom(SubThreadCommand.USER_COLLECTION_NAME + "/" + modId);
        edgeDocument.setTo(SubThreadCommand.THREAD_COLLECTION_NAME + "/" + threadName);
        arango.createEdgeDocument(SubThreadCommand.DB_Name, SubThreadCommand.USER_MOD_THREAD_COLLECTION_NAME, edgeDocument);
    }

    public static void insertReports(String reportId, String reporterId, String typeOfReport, String threadId,
                                     String reportMsg, String subthreadId) {
        BaseDocument report = new BaseDocument();
        report.setKey(reportId);
        report.addAttribute(SubThreadCommand.REPORTER_ID_DB, reporterId);
        report.addAttribute(SubThreadCommand.TYPE_OF_REPORT_DB, typeOfReport);
        report.addAttribute(SubThreadCommand.THREAD_ID_DB, threadId);
        report.addAttribute(SubThreadCommand.REPORT_MSG_DB, reportMsg);
        report.addAttribute(SubThreadCommand.SUBTHREAD_ID_DB, subthreadId);
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        report.addAttribute(SubThreadCommand.DATE_CREATED_DB, sqlDate2);

        addObjectToCollection(report, SubThreadCommand.SUBTHREAD_REPORTS_COLLECTION_NAME);
    }

    public static String moderatorSeeReports(String threadId) {
        ModeratorSeeReports moderatorSeeReports = new ModeratorSeeReports();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadCommand.THREAD_ID, threadId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);
        
        return moderatorSeeReports.execute(request);
    }

    @Test
    public void T01_ModeratorSeeReports() {

        String response = moderatorSeeReports(parentThreadId1);
        JSONObject responseJson = new JSONObject(response);
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(3, dataArr.length());
    }

    @Test
    public void T02_ModeratorSeeReports() {

        String response = moderatorSeeReports(parentThreadId2);
        JSONObject responseJson = new JSONObject(response);
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(0, dataArr.length());
    }
}
