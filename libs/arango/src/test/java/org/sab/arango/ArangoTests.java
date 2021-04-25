package org.sab.arango;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.*;

import java.util.HashMap;

import static org.junit.Assert.*;

public class ArangoTests {
    private static ArangoDB arangoDB;
    private static String dbName;
    private static String collectionName;
    private static String documentKey;
    private static HashMap<String, Object> documentProperties;

    @BeforeClass
    public static void prepareArango() {
        arangoDB = Arango.initializeConnection();
        dbName = "TestDB";
        collectionName = "TestCollection";
        documentKey = "key";

        documentProperties = new HashMap<>();
        documentProperties.put("boolean_field", true);
        documentProperties.put("int_field", 1);
        documentProperties.put("string_field", "kokowawa");

        assertTrue(Arango.createDatabase(arangoDB, dbName));
        assertTrue(arangoDB.getDatabases().contains(dbName));

        assertTrue(Arango.createCollection(arangoDB, dbName, collectionName));
        assertTrue(arangoDB.db(dbName).getCollections().stream().anyMatch(a -> a.getName().equals(collectionName)));
    }

    @AfterClass
    public static void cleanArango() {
        Arango.dropCollection(arangoDB, dbName, collectionName);
        assertFalse(arangoDB.db(dbName).getCollections().stream().anyMatch(a -> a.getName().equals(collectionName)));

        assertTrue(Arango.dropDatabase(arangoDB, dbName));
        assertFalse(arangoDB.getDatabases().contains(dbName));
    }

    @After
    public void cleanDB() {
        if (arangoDB.db(dbName).collection(collectionName).documentExists(documentKey))
            arangoDB.db(dbName).collection(collectionName).deleteDocument(documentKey);

        assertEquals(arangoDB.db(dbName).collection(collectionName).count().getCount().intValue(), 0);
    }

    @Test
    public void ArangoDocumentCreationTest() {
        BaseDocument baseDocument = new BaseDocument(documentProperties);
        baseDocument.setKey(documentKey);

        assertEquals(arangoDB.db(dbName).collection(collectionName).count().getCount().intValue(), 0);
        assertTrue(Arango.createDocument(arangoDB, dbName, collectionName, baseDocument));
        assertEquals(arangoDB.db(dbName).collection(collectionName).count().getCount().intValue(), 1);
    }

    @Test
    public void ArangoDocumentReadTest() {
        BaseDocument baseDocument = new BaseDocument(documentProperties);
        baseDocument.setKey(documentKey);

        Arango.createDocument(arangoDB, dbName, collectionName, baseDocument);

        baseDocument = Arango.readDocument(arangoDB, dbName, collectionName, documentKey);

        assertNotNull(baseDocument);
        assertEquals(baseDocument.getKey(), documentKey);
        assertEquals(baseDocument.getProperties(), documentProperties);
    }

    @Test
    public void ArangoDocumentReadAsJSONTest() {
        BaseDocument baseDocument = new BaseDocument(documentProperties);
        baseDocument.setKey(documentKey);

        Arango.createDocument(arangoDB, dbName, collectionName, baseDocument);

        ObjectNode obj = Arango.readDocumentAsJSON(arangoDB, dbName, collectionName, documentKey);

        assertNotNull(obj);
        for (String key : documentProperties.keySet()) {
            Object value = documentProperties.get(key);
            switch (value.getClass().getSimpleName()) {
                case "Boolean":
                    assertEquals(obj.get(key).asBoolean(), value);
                    break;

                case "Integer":
                    assertEquals(obj.get(key).asInt(), value);
                    break;

                case "String":
                    assertEquals(obj.get(key).asText(), value);
                    break;

                default:
                    fail();
            }
        }
    }

    @Test
    public void ArangoDocumentDeleteTest() {
        BaseDocument baseDocument = new BaseDocument(documentProperties);
        baseDocument.setKey(documentKey);

        Arango.createDocument(arangoDB, dbName, collectionName, baseDocument);

        assertEquals(arangoDB.db(dbName).collection(collectionName).count().getCount().intValue(), 1);
        assertTrue(Arango.deleteDocument(arangoDB, dbName, collectionName, documentKey));
        assertEquals(arangoDB.db(dbName).collection(collectionName).count().getCount().intValue(), 0);
    }
}
