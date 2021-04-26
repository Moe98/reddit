package org.sab.arango;

import com.arangodb.*;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.mapping.ArangoJack;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("unused")
public class Arango {
    private static Arango instance = null;
    private static ArangoDB.Builder builder;

    private Arango() {
        try {
            builder = new ArangoDB.Builder()
                    .user(System.getenv("ARANGO_USER"))
                    .password(System.getenv("ARANGO_PASSWORD"))
                    .serializer(new ArangoJack());
        } catch(ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }

    public static Arango getInstance() throws ArangoDBException{
        if (instance == null) {
            try {
                instance = new Arango();
            } catch (ArangoDBException e){
                throw new ArangoDBException(e);
            }
        }
        return instance;
    }

    public ArangoDB connect() throws  ArangoDBException {
        try {
            return builder.build();
        } catch (ArangoDBException e){
            throw new ArangoDBException(e);
        }
    }

    public void disconnect(ArangoDB arangoDB) throws  ArangoDBException {
        try {
            arangoDB.shutdown();
        } catch (ArangoDBException e){
            throw new ArangoDBException(e);
        }
    }


    public boolean createDatabase(ArangoDB arangoDB, String dbName) throws ArangoDBException{
        try {
            return arangoDB.createDatabase(dbName);
        } catch(ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }

    public boolean dropDatabase(ArangoDB arangoDB, String dbName) throws ArangoDBException{
        try {
            return arangoDB.db(dbName).drop();
        } catch(ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }

    public void createCollection(ArangoDB arangoDB, String dbName, String collectionName) throws ArangoDBException{
        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(collectionName);
        } catch(ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }

    public void dropCollection(ArangoDB arangoDB, String dbName, String collectionName) throws ArangoDBException{
        try {
            arangoDB.db(dbName).collection(collectionName).drop();
        } catch (ArangoDBException e){
            throw new ArangoDBException(e);
        }
    }

    public BaseDocument createDocument(ArangoDB arangoDB, String dbName, String collectionName, BaseDocument baseDocument) throws ArangoDBException{
        try {
            arangoDB.db(dbName).collection(collectionName).insertDocument(baseDocument);
            return readDocument(arangoDB, dbName, collectionName, baseDocument.getKey());
        } catch(ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }

    public BaseDocument readDocument(ArangoDB arangoDB, String dbName, String collectionName,String documentKey) throws ArangoDBException{
        try {
            return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, BaseDocument.class);
        } catch(ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }

    public BaseDocument updateDocument(ArangoDB arangoDB, String dbName, String collectionName, BaseDocument updatedDocument, String documentKey) throws ArangoDBException{
        try {
            arangoDB.db(dbName).collection(collectionName).updateDocument(documentKey, updatedDocument);
            return readDocument(arangoDB, dbName, collectionName, updatedDocument.getKey());
        } catch (ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }

    public boolean deleteDocument(ArangoDB arangoDB, String dbName, String collectionName,String documentKey) throws ArangoDBException{
        try {
            arangoDB.db(dbName).collection(collectionName).deleteDocument(documentKey);
            return true;
        } catch (ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }

    public ObjectNode readDocumentAsJSON(ArangoDB arangoDB, String dbName, String collectionName,String documentKey) throws ArangoDBException{
        try {
            return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, ObjectNode.class);
        } catch (ArangoDBException e) {
            throw new ArangoDBException(e);
        }
    }
}
