package org.sab.arango;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("unused")
public class Arango {
    final private static Arango instance = new Arango();
    private static ArangoDB.Builder builder;
    private ArangoDB arangoDB;
    private Arango() {

        final Properties properties = new Properties();
        int NUM_OF_CONNECTIONS = 10;
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            NUM_OF_CONNECTIONS = Integer.parseInt(properties.getProperty("NUMBER_OF_CONNECTIONS"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String ARANG_HOST = "127.0.0.1";
        if(System.getenv("ARANGO_HOST") != null)
            ARANG_HOST = System.getenv("ARANGO_HOST");
        builder = new ArangoDB.Builder()
                    .host(ARANG_HOST, 8529)
                    .user(System.getenv("ARANGO_USER"))
                    .password(System.getenv("ARANGO_PASSWORD"))
                    .maxConnections(NUM_OF_CONNECTIONS)
                    .serializer(new ArangoJack())
                    .connectionTtl(null)
                    .keepAliveInterval(600);
        
        arangoDB = builder.build();
    }

    public static Arango getInstance() {
        return instance;
    }

    private void connect() {
        arangoDB = builder.build();
    }

    private boolean isConnected() {
        return arangoDB != null && arangoDB.db().exists();
    }

    private void connectIfNotConnected() {
        if (!isConnected()){
            connect();
        }
    }

    private void disconnect() {
        if (arangoDB != null) {
            arangoDB.shutdown();
        }
    }

    public boolean createDatabase(String dbName) {
        return arangoDB.createDatabase(dbName);
    }

    public boolean dropDatabase(String dbName) {
        return arangoDB.db(dbName).drop();
    }

    public boolean databaseExists(String dbName) {
        return arangoDB.db(dbName).exists();
    }

    public void createDatabaseIfNotExists(String dbName) {
        if (!databaseExists(dbName))
            createDatabase(dbName);
    }

    public void createCollection(String dbName, String collectionName, boolean isEdgeCollection) {
        arangoDB.db(dbName).createCollection(collectionName, new CollectionCreateOptions().type(isEdgeCollection ? CollectionType.EDGES : CollectionType.DOCUMENT));
    }

    public void dropCollection(String dbName, String collectionName) {
        arangoDB.db(dbName).collection(collectionName).drop();
    }

    public boolean collectionExists(String dbName, String collectionName) {
        return arangoDB.db(dbName).collection(collectionName).exists();
    }

    public void createCollectionIfNotExists(String dbName, String collectionName, boolean isEdgeCollection) {
        if (!collectionExists(dbName, collectionName))
            createCollection(dbName, collectionName, isEdgeCollection);
    }

    public BaseDocument createDocument(String dbName, String collectionName, BaseDocument baseDocument) {
        arangoDB.db(dbName).collection(collectionName).insertDocument(baseDocument);
        return readDocument(dbName, collectionName, baseDocument.getKey());
    }

    public static BaseDocument createDocument(String dbName, String collectionName, Map<String, Object> properties, String key) {
        BaseDocument newDocument = new BaseDocument(new HashMap<>(properties));
        newDocument.setKey(key);
        Arango arango = getInstance();

        return arango.createDocument(dbName, collectionName, newDocument);
    }

    public BaseEdgeDocument createEdgeDocument(String dbName, String collectionName, BaseEdgeDocument baseEdgeDocument) {
        arangoDB.db(dbName).collection(collectionName).insertDocument(baseEdgeDocument);
        return readEdgeDocument(dbName, collectionName, baseEdgeDocument.getKey());
    }

    public BaseDocument readDocument(String dbName, String collectionName, String documentKey) {
        return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, BaseDocument.class);
    }

    public BaseEdgeDocument readEdgeDocument(String dbName, String collectionName, String documentKey) {
        return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, BaseEdgeDocument.class);
    }

    public BaseDocument updateDocument(String dbName, String collectionName, BaseDocument updatedDocument, String documentKey) {
        arangoDB.db(dbName).collection(collectionName).updateDocument(documentKey, updatedDocument);
        return readDocument(dbName, collectionName, updatedDocument.getKey());
    }

    public static BaseDocument updateDocument(String dbName, String collectionName, Map<String, Object> updatedProperties, String documentKey) {
        BaseDocument updatedDocument = new BaseDocument(new HashMap<>(updatedProperties));
        updatedDocument.setKey(documentKey);
        Arango arango = Arango.getInstance();

        return arango.updateDocument(dbName, collectionName, updatedDocument, documentKey);
    }

    public BaseEdgeDocument updateEdgeDocument(String dbName, String collectionName, BaseEdgeDocument updatedDocument, String documentKey) {
        arangoDB.db(dbName).collection(collectionName).updateDocument(documentKey, updatedDocument);
        return readEdgeDocument(dbName, collectionName, updatedDocument.getKey());
    }

    public boolean deleteDocument(String dbName, String collectionName, String documentKey) {
        arangoDB.db(dbName).collection(collectionName).deleteDocument(documentKey);
        return true;
    }

    public boolean documentExists(String dbName, String collectionName, String documentKey) {
        return arangoDB.db(dbName).collection(collectionName).documentExists(documentKey);
    }

    public ObjectNode readDocumentAsJSON(String dbName, String collectionName, String documentKey) {
        return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, ObjectNode.class);
    }

    public ViewEntity createView(String dbName, String viewName, String collectionName, String[] fields) {

        ArangoSearchCreateOptions options = new ArangoSearchCreateOptions();

        FieldLink[] fieldLinks = new FieldLink[fields.length];
        for (int i = 0; i < fields.length; i++) {
            FieldLink fieldLink = FieldLink.on(fields[i]);
            fieldLink.analyzers("text_en");
            fieldLinks[i] = fieldLink;
        }

        CollectionLink collectionLink = CollectionLink.on(collectionName);
        collectionLink.includeAllFields(true);
        collectionLink.fields(fieldLinks);

        options.link(collectionLink);
        return arangoDB.db(dbName).createArangoSearch(viewName, options);

    }

    public void dropView(String dbName, String viewName) {
        arangoDB.db(dbName).view(viewName).drop();
    }

    public boolean viewExists(String dbName, String viewName) {
        return arangoDB.db(dbName).view(viewName).exists();
    }

    public void createViewIfNotExists(String dbName, String viewName, String collectionName, String[] fields) {
        if (!viewExists(dbName, viewName)) {
            createView(dbName, viewName, collectionName, fields);
        }
    }

    public ArangoCursor<BaseDocument> query(String dbName, String query, Map<String, Object> bindVars) {
        return arangoDB.db(dbName).query(query, bindVars, null, BaseDocument.class);
    }

    public String getSingleEdgeId(String DB_Name, String collectionName, String fromNodeId, String toNodeId){
        String query = """
                FOR node, edge IN 1..1 OUTBOUND @fromNodeId @collectionName
                    FILTER node._id == @toNodeId
                    RETURN DISTINCT {edgeId:edge._key}
                """;

        Map<String, Object> bindVars =  new HashMap<>();
        bindVars.put("fromNodeId", fromNodeId);
        bindVars.put("toNodeId", toNodeId);
        bindVars.put("collectionName", collectionName);
        // TODO: System.getenv("ARANGO_DB") instead of writing the DB
        ArangoCursor<BaseDocument> cursor = query(DB_Name, query, bindVars);
        String edgeId = "";
        if (cursor.hasNext()) {
            edgeId = (String) cursor.next().getAttribute("edgeId");
        }
        return edgeId;
    }

    
    public boolean containsDatabase(String dbName) {
        return arangoDB.getDatabases().contains(dbName);
    }

    public boolean containsCollection(String dbName, String collectionName) {
        return arangoDB.db(dbName).getCollections().stream().anyMatch(a -> a.getName().equals(collectionName));
    }

    public int documentCount(String dbName, String collectionName) {
        return arangoDB.db(dbName).collection(collectionName).count().getCount().intValue();
    }

    public ArangoCursor<BaseDocument> filterCollection(String DB_Name, String collectionName, String attributeName, String attributeValue){
        JSONArray data = new JSONArray();
        String query = """
                    FOR obj IN %s
                        FILTER obj.%s == @attributeValue
                        RETURN obj    
                    """.formatted(collectionName,attributeName);

        Map<String, Object> bindVars =  new HashMap<>();
        bindVars.put("attributeValue", attributeValue);
        return query(DB_Name, query, bindVars);
    }

    public ArangoCursor<BaseDocument> filterEdgeCollection(String DB_Name, String collectionName, String fromNodeId){
        String query = """
                    FOR node IN 1..1 OUTBOUND @fromNodeId @collectionName
                    RETURN node
                    """;

        Map<String, Object> bindVars =  new HashMap<>();
        bindVars.put("fromNodeId", fromNodeId);
        bindVars.put("collectionName", collectionName);
        return query(DB_Name, query, bindVars);
    }

    public JSONArray parseOutput(ArangoCursor<BaseDocument> cursor, String keyName, ArrayList<String> attributeNames) {
        JSONArray data = new JSONArray();
        cursor.forEachRemaining(document -> {
            JSONObject object = new JSONObject();
            for(String attribute : attributeNames) {
                object.put(attribute, document.getProperties().get(attribute));
            }
            object.put(keyName, document.getKey());
            data.put(object);
        });
        return data;
    }

    public ArangoCursor<BaseDocument> filterEdgeCollectionInbound(String DB_Name, String collectionName, String toNodeId){
        String query = """
                    FOR node IN 1..1 INBOUND @fromNodeId @collectionName
                    RETURN node
                    """;

        Map<String, Object> bindVars =  new HashMap<>();
        bindVars.put("fromNodeId", toNodeId);
        bindVars.put("collectionName", collectionName);
        return query(DB_Name, query, bindVars);
    }
}
