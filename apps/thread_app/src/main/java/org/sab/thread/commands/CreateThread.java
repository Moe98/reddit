package org.sab.thread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.Thread;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;
import java.util.List;

public class CreateThread extends ThreadCommand {

    @Override
    protected Schema getSchema() {
        Attribute threadName = new Attribute(THREAD_NAME, DataType.STRING, true);
        Attribute description = new Attribute(DESCRIPTION, DataType.STRING, true);
        Attribute creatorId = new Attribute(CREATOR_ID, DataType.STRING, true);
        Attribute dateCreated = new Attribute(DATE_CREATED, DataType.SQL_DATE, true);
        Attribute numOfFollowers = new Attribute(NUM_OF_FOLLOWERS, DataType.INT);

        return new Schema(List.of(threadName, description, creatorId, dateCreated, numOfFollowers));
    }

    private Arango arango;
    private ArangoDB arangoDB;
    private String collectionName;
    private String DBName;

    @Override
    public String execute() {
        String name = body.getString(THREAD_NAME);
        String description = body.getString(DESCRIPTION);
        String creatorId = body.getString(CREATOR_ID);
        String date = body.getString(DATE_CREATED);
        long numOfFollowers = 0;

        Thread thread = new Thread();

        collectionName = "Thread";
        DBName = "ARANGO_DB";

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, collectionName)) {
                arango.createCollection(arangoDB, DBName, collectionName, false);
            }
            // TODO:
            BaseDocument myObject = new BaseDocument();
            myObject.setKey(name);
            myObject.addAttribute("Name", name);
            myObject.addAttribute("Description", description);
            myObject.addAttribute("CreatorId", creatorId);
            myObject.addAttribute("DateCreated", date);
            myObject.addAttribute("NumOfFollowers", numOfFollowers);

            BaseDocument res = arango.createDocument(arangoDB, DBName, collectionName, myObject);

            thread.setName((String) res.getAttribute("Name"));
            thread.setDescription((String) res.getAttribute("Description"));
            thread.setCreatorId((String) res.getAttribute("CreatorId"));
            thread.setDateCreated((String) res.getAttribute("DateCreated"));
            thread.setNumOfFollowers(Integer.parseInt(res.getAttribute("NumOfFollowers").toString()));

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
        return Responder.makeDataResponse(thread.toJSON()).toString();
    }




    public static void main(String[] args) {
        CreateThread tc = new CreateThread();
        JSONObject request = new JSONObject("{\"body\":{\"dateCreated\":\"1998-2-9\",\"name\":\"asmakElRayes7amido\",\"creatorId\":\"sd54sdsda\",\"description\":\"agmad subreddit fl wogod\"},\"uriParams\":{},\"methodType\":\"POST\"}");
        System.out.println(tc.execute(request));
    }

}
