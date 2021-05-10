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

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Arango {
    final private static Arango instance = new Arango();
    private static ArangoDB.Builder builder;
    private ArangoDB arangoDB;

    private Arango() {
        builder = new ArangoDB.Builder()
                .user(System.getenv("ARANGO_USER"))
                .password(System.getenv("ARANGO_PASSWORD"))
                .serializer(new ArangoJack())
                .connectionTtl(null)
                .keepAliveInterval(600);

        connect();
    }

    public static Arango getInstance() {
        return instance;
    }

    private void connect() {
        arangoDB = builder.build();
    }

    public boolean isConnected() {
        return arangoDB != null && arangoDB.db().exists();
    }

    public void connectIfNotConnected() {
        if (!isConnected())
            connect();
    }

    public void disconnect() {
        if (arangoDB != null) {
            arangoDB.shutdown();
            arangoDB = null;
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

    public static String getSingleEdgeId(String dbName, String collectionName, String userId, String contentId){
        String query = """
                FOR content, edge IN 1..1 OUTBOUND @username @collectionName
                    FILTER content._id == @contentId
                    RETURN DISTINCT {edgeId:edge._key}
                """;

        Map<String, Object> bindVars =  new HashMap<>();
        bindVars.put("username", userId);
        bindVars.put("contentId", contentId);
        bindVars.put("collectionName", collectionName);

        ArangoCursor<BaseDocument> cursor = instance.query(dbName, query, bindVars);
        String edgeId = "";
        
        if (cursor.hasNext()) {
            edgeId = (String)cursor.next().getAttribute("edgeId");
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
}
