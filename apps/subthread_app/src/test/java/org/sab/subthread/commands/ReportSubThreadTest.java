package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.service.validation.HTTPMethod;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ReportSubThreadTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String parentThreadId1 = "asmakElRayes7amido", title1 = "gelaty azza is better", content1 = "fish is ya3", hasImage1 = "false";
    final private static String parentThreadId2 = "GelatiAzza", title2 = "fish is better", content2 = "fish is better", hasImage2 = "false";
    final private static String subthreadId1 = "20001", subthreadId2 = "20002", subthreadId3 = "20003";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine, thread1, thread2;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getConnectedInstance();

            arango.createDatabaseIfNotExists(SubThreadCommand.DB_Name);
            createUsers();
            createThreads();
            createSubThread(subthreadId1, parentThreadId1, content1, mantaId, title1, hasImage1);
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
    }

    public static String reportSubthread(String reporterId, String typeOfReport,
                                         String reportedSubthredId, String threadId, String reportMsg) {
        ReportSubThread tc = new ReportSubThread();

        JSONObject body = new JSONObject();
        body.put(SubThreadCommand.TYPE_OF_REPORT, typeOfReport);
        body.put(SubThreadCommand.REPORTED_SUBTHREAD_ID, reportedSubthredId);
        body.put(SubThreadCommand.THREAD_ID, threadId);
        body.put(SubThreadCommand.REPORT_MSG, reportMsg);

        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadCommand.REPORTER_ID, reporterId);

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.POST);

        JSONObject claims = new JSONObject().put(SubThreadCommand.USERNAME, reporterId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        return tc.execute(request);
    }

    @Test
    public void T01_UserReportingRealSubThread() {

        String typeOfReport = "SCAM";
        String reportedSubthreadId = subthreadId1;
        String threadId = parentThreadId1;
        String reportMsg = "ban this scammer naw!";
        String response = reportSubthread(mantaId, typeOfReport, reportedSubthreadId, threadId, reportMsg);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONObject data = (JSONObject) (responseJson.get("data"));
        assertEquals("Created Subthread Report", data.get("msg"));


        ArangoCursor<BaseDocument> cursor = arango.filterCollection(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_REPORTS_COLLECTION_NAME, SubThreadCommand.REPORTER_ID_DB, mantaId);
        ArrayList<String> reportAtt = new ArrayList<>();
        reportAtt.add(SubThreadCommand.REPORTER_ID_DB);
        reportAtt.add(SubThreadCommand.TYPE_OF_REPORT_DB);
        reportAtt.add(SubThreadCommand.THREAD_ID_DB);
        reportAtt.add(SubThreadCommand.DATE_CREATED_DB);
        reportAtt.add(SubThreadCommand.REPORT_MSG_DB);
        reportAtt.add(SubThreadCommand.REPORTED_SUBTHREAD_ID);
        JSONArray reportArr = arango.parseOutput(cursor, SubThreadCommand.REPORT_ID_DB, reportAtt);
        assertEquals(1, reportArr.length());
        assertEquals(mantaId, ((JSONObject) reportArr.get(0)).get(SubThreadCommand.REPORTER_ID_DB));
        assertEquals(typeOfReport, ((JSONObject) reportArr.get(0)).get(SubThreadCommand.TYPE_OF_REPORT_DB));
        assertEquals(threadId, ((JSONObject) reportArr.get(0)).get(SubThreadCommand.THREAD_ID_DB));
        assertEquals(reportMsg, ((JSONObject) reportArr.get(0)).get(SubThreadCommand.REPORT_MSG_DB));
        // TODO test fails
        assertEquals(reportedSubthreadId, ((JSONObject) reportArr.get(0)).get(SubThreadCommand.REPORTED_SUBTHREAD_ID));
    }

    @Test
    public void T02_UserReportingSubThreadFromNonExistingThread() {

        String typeOfReport = "SCAM";
        String reportedSubthreadId = subthreadId1;
        String threadId = parentThreadId2;
        String reportMsg = "ban this scammer naw!";
        String response = reportSubthread(mantaId, typeOfReport, reportedSubthreadId, threadId, reportMsg);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(400, responseJson.getInt("statusCode"));
        assertEquals("Thread does not exist", responseJson.get("msg"));
    }

    @Test
    public void T03_UserReportingNonExistingSubThread() {

        String typeOfReport = "SCAM";
        String reportedSubthreadId = subthreadId2;
        String threadId = parentThreadId1;
        String reportMsg = "ban this scammer naw!";
        String response = reportSubthread(mantaId, typeOfReport, reportedSubthreadId, threadId, reportMsg);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(400, responseJson.getInt("statusCode"));
        assertEquals("Subthread does not exist", responseJson.get("msg"));
    }
}
