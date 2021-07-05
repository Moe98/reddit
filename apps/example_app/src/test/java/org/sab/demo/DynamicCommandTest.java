package org.sab.demo;

import kotlin.Pair;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.sab.classes.Reader;
import org.sab.service.managers.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DynamicCommandTest {

    private static Pair<ExampleApp, InvocationManager> startMockApp() throws ReflectiveOperationException, IOException,
            TimeoutException {
        final ExampleApp app = new ExampleApp();

        final ClassManager classManager = new ClassManager();
        final ThreadPoolManager threadPoolManager = new ThreadPoolManager();
        final InvocationManager invocationManager = new InvocationManager(threadPoolManager, classManager);

        final QueueManager queueManager = Mockito.spy(new QueueManager(app.getAppUriName(), invocationManager));
        Mockito.doNothing().when(queueManager).initAcceptingNewRequests();
        Mockito.doNothing().when(queueManager).startAcceptingNewRequests();

        final ControlManager controlManager = new ControlManager(app.getAppUriName());

        editPrivateField(controlManager, "threadPoolManager", threadPoolManager);
        editPrivateField(controlManager, "classManager", classManager);
        editPrivateField(controlManager, "queueManager", queueManager);

        editPrivateField(app, "controlManager", controlManager);
        app.start();

        return new Pair<>(app, invocationManager);
    }

    private static void editPrivateField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        final Field field;
        Field temp;
        try {
            temp = obj.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            temp = obj.getClass().getSuperclass().getDeclaredField(fieldName);
        }

        field = temp;
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    public void addCommand() {
        try {
            final Pair<ExampleApp, InvocationManager> pair = startMockApp();
            final ExampleApp app = pair.getFirst();
            final InvocationManager invocationManager = pair.getSecond();

            final String functionName = "MORNING_WORLD";
            final String invocationResult = invocationManager.invokeCommand(functionName, new JSONObject());
            assertEquals(
                    "{\"statusCode\": 404, \"msg\": \"Function-Name class: (" + functionName + ") not found\"}",
                    invocationResult
            );

            app.getControlManager().addCommandWithBytes(
                    functionName,
                    "org.sab.demo.commands.MorningWorld",
                    Reader.readBytesFromResource("MorningWorld")
            );

            final String afterAdditionResult = invocationManager.invokeCommand(functionName, new JSONObject());
            assertEquals(
                    "{\"msg\":\"Morning World\", \"statusCode\": 200}",
                    afterAdditionResult
            );
        } catch (ReflectiveOperationException | IOException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void updateCommand() {
        try {
            final Pair<ExampleApp, InvocationManager> pair = startMockApp();
            final ExampleApp app = pair.getFirst();
            final InvocationManager invocationManager = pair.getSecond();

            final String functionName = "HELLO_WORLD";
            final String invocationResult = invocationManager.invokeCommand(functionName, new JSONObject());
            assertEquals(
                    "{\"msg\":\"Hello World\", \"statusCode\": 200}",
                    invocationResult
            );

            app.getControlManager().updateCommandWithBytes(
                    functionName,
                    "org.sab.demo.commands.HelloWorld",
                    Reader.readBytesFromResource("ImprovedHello")
            );

            final String afterUpdateResult = invocationManager.invokeCommand(functionName, new JSONObject());
            assertEquals(
                    "{\"msg\":\"Hi\", \"statusCode\": 200}",
                    afterUpdateResult
            );
        } catch (ReflectiveOperationException | IOException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void deleteCommand() {
        try {
            final Pair<ExampleApp, InvocationManager> pair = startMockApp();
            final ExampleApp app = pair.getFirst();
            final InvocationManager invocationManager = pair.getSecond();

            final String functionName = "HELLO_WORLD";
            final String invocationResult = invocationManager.invokeCommand(functionName, new JSONObject());
            assertEquals("{\"msg\":\"Hello World\", \"statusCode\": 200}", invocationResult);

            app.getControlManager().deleteCommand(functionName);

            final String afterDeletionResult = invocationManager.invokeCommand(functionName, new JSONObject());
            assertEquals(
                    "{\"statusCode\": 404, \"msg\": \"Function-Name class: (" + functionName + ") not found\"}",
                    afterDeletionResult
            );
        } catch (ReflectiveOperationException | IOException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

}
