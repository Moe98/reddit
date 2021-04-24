package org.sab.arango;

import com.arangodb.*;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.mapping.ArangoJack;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.Map;

public class Arango {
    public static ArangoDB initializeConnection(){
        ArangoDB arangoDB = new ArangoDB.Builder().password("koko")
                .serializer(new ArangoJack())
                .build();
        return arangoDB;
    }

    public static Boolean createDatabase( ArangoDB arangoDB ,String dbName){
            return arangoDB.createDatabase(dbName);
    }

    public static Boolean createCollection(ArangoDB arangoDB, String dbName, String collectionName){
        CollectionEntity collectionEntity = arangoDB.db(dbName).createCollection(collectionName);
        return (collectionEntity.getName().equals(collectionName));
    }

    public static Boolean createDocument(ArangoDB arangoDB, String dbName, String collectionName, BaseDocument baseDocument){
        arangoDB.db(dbName).collection(collectionName).insertDocument(baseDocument);
        return arangoDB.db(dbName).collection(collectionName).documentExists(baseDocument.getKey());
    }

    public static BaseDocument readDocument(ArangoDB arangoDB, String dbName, String collectionName,String documentKey){
        return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, BaseDocument.class);
    }

    public static ObjectNode readDocumentAsJSON(ArangoDB arangoDB, String dbName, String collectionName,String documentKey){
        return arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, ObjectNode.class);
    }

    public static Boolean updateDocument(ArangoDB arangoDB, String dbName, String collectionName, BaseDocument baseDocument,String documentKey){
        arangoDB.db(dbName).collection(collectionName).updateDocument(documentKey, baseDocument);
        return (arangoDB.db(dbName).collection(collectionName).getDocument(documentKey, BaseDocument.class).equals(baseDocument));
    }

    public static Boolean deleteDocument(ArangoDB arangoDB, String dbName, String collectionName,String documentKey){
        arangoDB.db(dbName).collection(collectionName).deleteDocument(documentKey);
        return !(arangoDB.db(dbName).collection(collectionName).documentExists(documentKey));
    }
}
