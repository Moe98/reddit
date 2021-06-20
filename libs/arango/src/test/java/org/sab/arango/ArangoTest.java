package org.sab.arango;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.*;
import org.sab.databases.PoolDoesNotExistException;

import java.util.HashMap;

import static org.junit.Assert.*;

public class ArangoTest {
    private static Arango arango;
    private static String dbName;
    private static String collectionName;
    private static HashMap<String, Object> documentProperties;
    private static String viewName;

    final static int CONNECTION_COUNT = 10;


    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.createPool(10);


            dbName = "TestDB";
            collectionName = "TestCollection";
            viewName = "TestView";
            documentProperties = new HashMap<>();
            documentProperties.put("boolean_field", true);
            documentProperties.put("int_field", 1);
            documentProperties.put("string_field", "kokowawa");

            assertTrue(arango.createDatabase(dbName));
            assertTrue(arango.containsDatabase(dbName));

            if (arango.containsCollection(dbName, collectionName))
                arango.dropCollection(dbName, collectionName);
            assertFalse(arango.containsCollection(dbName, collectionName));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        try {
            assertTrue(arango.dropDatabase(dbName));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
        try {
            arango.destroyPool();
        } catch (PoolDoesNotExistException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void buildCollection() {
        try {
            arango.createCollection(dbName, collectionName, false);
            assertTrue(arango.containsCollection(dbName, collectionName));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void dropCollection() {
        try {
            arango.dropCollection(dbName, collectionName);
            assertFalse(arango.containsCollection(dbName, collectionName));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void databaseExists() {
        try {
            arango.createDatabase("testDB");
            assertTrue(arango.databaseExists("testDB"));
            arango.dropDatabase("testDB");
            assertFalse(arango.databaseExists("testDB"));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void collectionExists() {
        try {
            arango.createCollection(dbName, "testCollectionExists", false);
            assertTrue(arango.collectionExists(dbName, "testCollectionExists"));
            arango.dropCollection(dbName, "testCollectionExists");
            assertFalse(arango.collectionExists(dbName, "testCollectionExists"));
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
            assertEquals(arango.documentCount(dbName, collectionName), 0);
            assertNotNull(arango.createDocument(dbName, collectionName, baseDocument));
            assertEquals(arango.documentCount(dbName, collectionName), 1);
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void readDocument() {
        try {
            BaseDocument baseDocument = new BaseDocument(documentProperties);
            baseDocument.setKey("read");
            arango.createDocument(dbName, collectionName, baseDocument);
            BaseDocument result = arango.readDocument(dbName, collectionName, "read");
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
            arango.createDocument(dbName, collectionName, baseDocument);
            ObjectNode obj = arango.readDocumentAsJSON(dbName, collectionName, "readJson");
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
            arango.createDocument(dbName, collectionName, baseDocument);
            baseDocument.updateAttribute("string_field", "updated");
            BaseDocument updated = arango.updateDocument(dbName, collectionName, baseDocument, "update");
            assertEquals("updated", updated.getProperties().get("string_field"));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void deleteDocument() {
        try {
            int currentDocsNum = arango.documentCount(dbName, collectionName);
            BaseDocument baseDocument = new BaseDocument(documentProperties);
            baseDocument.setKey("delete");
            arango.createDocument(dbName, collectionName, baseDocument);
            assertTrue(arango.deleteDocument(dbName, collectionName, "delete"));
            assertEquals(arango.documentCount(dbName, collectionName), currentDocsNum);
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void createView() {
        try {
            assertNotNull(arango.createView(dbName, "CreateViewTest", collectionName, new String[]{"string_field"}));
            assertTrue(arango.viewExists(dbName, "CreateViewTest"));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void dropView() {
        try {
            assertNotNull(arango.createView(dbName, "DropViewTest", collectionName, new String[]{"string_field"}));
            assertTrue(arango.viewExists(dbName, "DropViewTest"));
            arango.dropView(dbName, "DropViewTest");
            assertFalse(arango.viewExists(dbName, "DropViewTest"));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void viewExists() {
        try {
            assertFalse(arango.viewExists(dbName, viewName));
            arango.createView(dbName, viewName, collectionName, new String[]{});
            assertTrue(arango.viewExists(dbName, viewName));
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void query() {
        try {
            ArangoCursor<BaseDocument> cursor = arango.query(dbName, "RETURN { number: 1 }", null);
            assertTrue(cursor.hasNext());
            cursor.next();
            assertFalse(cursor.hasNext());
        } catch (ArangoDBException e) {
            fail(e.getMessage());
        }
    }
}