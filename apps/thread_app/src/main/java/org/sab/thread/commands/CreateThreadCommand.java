package org.sab.thread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.Thread;
import org.sab.service.Command;

public class CreateThreadCommand extends Command {
    private Arango arango;
    private ArangoDB arangoDB;
    private String collectionName;
    private String DBName;

    public static void main(String[] args) {
        CreateThreadCommand tc = new CreateThreadCommand();
        JSONObject request = new JSONObject("{\"body\":{\"dateCreated\":\"1998-22-9\",\"name\":\"klklk\",\"creatorId\":\"sd54sdsda\",\"description\":\"agmad subreddit fl wogod\"}}");
        System.out.println(tc.execute(request));
    }

    @Override
    public String execute(JSONObject request) {
        JSONObject body = request.getJSONObject("body");
        String name = body.getString("name");
        String description = body.getString("description");
        String creatorId = body.getString("creatorId");
        String date = body.getString("dateCreated");
        long numOfFollowers = 0;
        Thread thread = new Thread();

        JSONObject response = new JSONObject();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();
            collectionName = "Thread";
            DBName = "ARANGO_DB";

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, collectionName)) {
                arango.createCollection(arangoDB, DBName, collectionName, false);
            }

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
            thread.setNumOfFollowers(Long.parseLong(res.getAttribute("NumOfFollowers").toString()));

        } catch (Exception e) {
            System.err.println(e);
        } finally {
            arango.disconnect(arangoDB);
        }
        return thread.toJSON().toString();
    }
}
