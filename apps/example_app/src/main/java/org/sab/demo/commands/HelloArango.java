package org.sab.demo.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.HashMap;
import java.util.Map;


public class HelloArango extends CommandWithVerification {

    @Override
    protected String execute() {
        // Return SUCCESS
        String DB_NAME = System.getenv("ARANGO_DB");
        String COLLECTION_NAME = "Hello";

        Arango arango = Arango.getInstance();

        arango.createCollectionIfNotExists( DB_NAME, COLLECTION_NAME, false);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hello", true);
        properties.put("bye", "not bye");
        BaseDocument helloDoc = new BaseDocument(properties);

        arango.createDocument(DB_NAME, COLLECTION_NAME, helloDoc);

        return "{\"msg\":\"Successfully inserted Hello\", \"statusCode\": 200}";
    }

    @Override
    protected Schema getSchema() {
        return Schema.emptySchema();
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }
}