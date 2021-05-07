package org.sab.arango;

import com.arangodb.*;
import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.CollectionLink;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.mapping.ArangoJack;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

@SuppressWarnings("unused")
public class Arango {
    private static Arango instance = null;
    private static ArangoDB.Builder builder;

    private Arango() {
        builder = new ArangoDB.Builder()
                .user(System.getenv("ARANGO_USER"))
                .password(System.getenv("ARANGO_PASSWORD"))
                .serializer(new ArangoJack());
    }

    public static Arango getInstance() {
        if (instance == null) {
            instance = new Arango();
        }
        return instance;
    }

    public ArangoDB connect() {
        return builder.build();
    }

    public void disconnect(ArangoDB arangoDB) {
        arangoDB.shutdown();
    }

    public boolean createDatabase(ArangoDB arangoDB, String dbName) {
        return arangoDB.createDatabase(dbName);
    }

    public boolean dropDatabase(ArangoDB arangoDB, String dbName) {
        return arangoDB.db(dbName).drop();
    }

    public boolean databaseExists(ArangoDB arangoDB, String dbName) {
        return arangoDB.db(dbName).exists();
    }

    public void createDatabaseIfNotExists(ArangoDB arangoDB, String dbName) {
        if (!databaseExists(arangoDB, dbName))
            createDatabase(arangoDB, dbName);
    }

    public void createCollection(ArangoDB arangoDB, String dbName, String collectionName, boolean isEdgeCollection) {
        arangoDB.db(dbName).createCollection(collectionName, new CollectionCreateOptions().type(isEdgeCollection ? CollectionType.EDGES : CollectionType.DOCUMENT));
    }

    public void dropCollection(ArangoDB arangoDB, String dbName, String collectionName) {
        arangoDB.db(dbName).collection(collectionName).drop();
    }

    public boolean collectionExists(ArangoDB arangoDB, String dbName, String collectionName) {
        return arangoDB.db(dbName).collection(collectionName).exists();
    }

    public void createCollectionIfNotExists(ArangoDB arangoDB, String dbName, String collectionName, boolean isEdgeCollection) {
        if (!collectionExists(arangoDB, dbName, collectionName))
            createCollection(arangoDB, dbName, collectionName, isEdgeCollection);
    }

    public BaseDocument createDocument(ArangoDB arangoDB, String dbName, String collectionName, BaseDocument baseDocument) {
        arangoDB.db(dbName).collection(collectionName).insertDocument(baseDocument);
        return readDocument(arangoDB, dbName, collectionName, baseDocument.getKey());
    }

    public BaseEdgeDocument createEdgeDocument(ArangoDB arangoDB, String dbName, String collectionName, BaseEdgeDocument baseEdgeDocument) {
        arangoDB.db(dbName).collection(collectionName).insertDocument(baseEdgeDocument);
        return readEdgeDocument(arangoDB, dbName, collectionName, baseEdgeDocument.getKey());
    }

    public BaseDocument readDocument(ArangoDB arangoDB, String dbName, String collectionName, String documentKey) {
        return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, BaseDocument.class);
    }

    public BaseEdgeDocument readEdgeDocument(ArangoDB arangoDB, String dbName, String collectionName, String documentKey) {
        return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, BaseEdgeDocument.class);
    }

    public BaseDocument updateDocument(ArangoDB arangoDB, String dbName, String collectionName, BaseDocument updatedDocument, String documentKey) {
        arangoDB.db(dbName).collection(collectionName).updateDocument(documentKey, updatedDocument);
        return readDocument(arangoDB, dbName, collectionName, updatedDocument.getKey());
    }

    public BaseEdgeDocument updateEdgeDocument(ArangoDB arangoDB, String dbName, String collectionName, BaseEdgeDocument updatedDocument, String documentKey) {
        arangoDB.db(dbName).collection(collectionName).updateDocument(documentKey, updatedDocument);
        return readEdgeDocument(arangoDB, dbName, collectionName, updatedDocument.getKey());
    }

    public boolean deleteDocument(ArangoDB arangoDB, String dbName, String collectionName, String documentKey) {
        arangoDB.db(dbName).collection(collectionName).deleteDocument(documentKey);
        return true;
    }

    public boolean documentExists(ArangoDB arangoDB, String dbName, String collectionName, String documentKey) {
        return arangoDB.db(dbName).collection(collectionName).documentExists(documentKey);
    }

    public ObjectNode readDocumentAsJSON(ArangoDB arangoDB, String dbName, String collectionName, String documentKey) {
        return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, ObjectNode.class);
    }

    public ViewEntity createView(ArangoDB arangoDB, String dbName, String viewName, String collectionName, String[] fields) {

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

    public void dropView(ArangoDB arangoDB, String dbName, String viewName) {
        arangoDB.db(dbName).view(viewName).drop();
    }

    public boolean viewExists(ArangoDB arangoDB, String dbName, String viewName) {
        return arangoDB.db(dbName).view(viewName).exists();
    }

    public void createViewIfNotExists(ArangoDB arangoDB, String dbName, String viewName, String collectionName, String[] fields) {
        if (!viewExists(arangoDB, dbName, viewName)) {
            createView(arangoDB, dbName, viewName, collectionName, fields);
        }
    }

    public ArangoCursor<BaseDocument> query(ArangoDB arangoDB, String dbName, String query, Map<String, Object> bindVars) {
        return arangoDB.db(dbName).query(query, bindVars, null, BaseDocument.class);
    }
}