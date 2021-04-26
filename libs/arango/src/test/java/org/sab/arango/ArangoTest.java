package org.sab.arango;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class ArangoTest {
    private Arango arango;
    private ArangoDB arangoDB;
    private String dbName;
    private String collectionName;
    private HashMap<String, Object> documentProperties;


    @Before
    public void setUp() {
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            dbName = "TestDB";
            collectionName = "TestCollection";
            documentProperties = new HashMap<>();
            documentProperties.put("boolean_field", true);
            documentProperties.put("int_field", 1);
            documentProperties.put("string_field", "kokowawa");

            assertTrue(arango.createDatabase(arangoDB, dbName));
            assertTrue(arangoDB.getDatabases().contains(dbName));
            arango.createCollection(arangoDB, dbName, collectionName);
            assertTrue(arangoDB.db(dbName).getCollections().stream().anyMatch(a -> a.getName().equals(collectionName)));
        } catch (ArangoDBException e){
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            arango.dropCollection(arangoDB, dbName, collectionName);
            assertFalse(arangoDB.db(dbName).getCollections().stream().anyMatch(a -> a.getName().equals(collectionName)));
            arango.dropDatabase(arangoDB, dbName);
            arango.disconnect(arangoDB);
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getInstance() {
        try {
            Arango arango1 = Arango.getInstance();
            assertSame(arango1, arango);
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void createDocument() {
        try {
            BaseDocument baseDocument = new BaseDocument(documentProperties);
            baseDocument.setKey("create");
            assertEquals(arangoDB.db(dbName).collection(collectionName).count().getCount().intValue(), 0);
            assertNotNull(arango.createDocument(arangoDB, dbName, collectionName, baseDocument));
            assertEquals(arangoDB.db(dbName).collection(collectionName).count().getCount().intValue(), 1);
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void readDocument() {
        try {
            BaseDocument baseDocument = new BaseDocument(documentProperties);
            baseDocument.setKey("read");
            arango.createDocument(arangoDB, dbName, collectionName, baseDocument);
            BaseDocument result = arango.readDocument(arangoDB, dbName, collectionName, "read");
            assertNotNull(result);
            assertEquals(result.getKey(), "read");
            assertEquals(result.getProperties(), documentProperties);
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void readDocumentAsJSON() {
        try {
            BaseDocument baseDocument = new BaseDocument(documentProperties);
            baseDocument.setKey("readJson");
            arango.createDocument(arangoDB, dbName, collectionName, baseDocument);
            ObjectNode obj = arango.readDocumentAsJSON(arangoDB, dbName, collectionName, "readJson");
            assertNotNull(obj);
            for (String key : documentProperties.keySet()) {
                Object value = documentProperties.get(key);
                switch (value.getClass().getSimpleName()) {
                    case "Boolean" -> assertEquals(obj.get(key).asBoolean(), value);
                    case "Integer" -> assertEquals(obj.get(key).asInt(), value);
                    case "String" -> assertEquals(obj.get(key).asText(), value);
                    default -> fail("Invalid Object");
                }
            }
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void updateDocument() {
        try {
            BaseDocument baseDocument = new BaseDocument(documentProperties);
            baseDocument.setKey("update");
            arango.createDocument(arangoDB, dbName, collectionName, baseDocument);
            baseDocument.updateAttribute("string_field", "updated");
            BaseDocument updated = arango.updateDocument(arangoDB, dbName, collectionName, baseDocument, "update");
            assertTrue(updated.getProperties().get("string_field").equals("updated"));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void deleteDocument() {
        try {
            int currentDocsNum = arangoDB.db(dbName).collection(collectionName).count().getCount().intValue();
            BaseDocument baseDocument = new BaseDocument(documentProperties);
            baseDocument.setKey("delete");
            arango.createDocument(arangoDB, dbName, collectionName, baseDocument);
            assertTrue(arango.deleteDocument(arangoDB, dbName, collectionName, "delete"));
            assertEquals(arangoDB.db(dbName).collection(collectionName).count().getCount().intValue(), currentDocsNum);
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }
}