package org.sab.demo;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.sab.HttpServerUtilities.HttpClient;
import org.sab.demo.commands.GoodByeWorld;
import org.sab.demo.commands.HelloWorld;
import org.sab.netty.Server;
import org.sab.service.Service;
import org.sab.service.managers.ControlManager;
import org.sab.service.managers.PropertiesManager;
import org.sab.service.managers.ThreadPoolManager;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.cert.CertificateException;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Unit test for Example App.
 */
public class ExampleAppTest {
    public static int availablePort = 4000;


    @BeforeClass
    public static void runServer() {
        new Thread(() -> {
            try {
                Server.main(null);
            } catch (CertificateException | SSLException | InterruptedException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }

        }).start();
    }

    /**
     * A function to get the {@code threadPool} from the app even though it's private
     */
    private static ExecutorService getThreadPool(ExampleApp app) throws NoSuchFieldException, IllegalAccessException {
        Field field = ThreadPoolManager.class.getDeclaredField("threadPool");
        field.setAccessible(true);
        return (ExecutorService) field.get(app.getControlManager().getThreadPoolManager());

    }

    /**
     * Creates a mocked app that uses the controller port specified by {@link #obtainPort}.
     * @return the mocked app.
     */
    private static ExampleApp mockApp() throws ReflectiveOperationException {
        final int controllerPort = obtainPort();

        final ExampleApp app = new ExampleApp();

        final PropertiesManager propertiesManager = Mockito.spy(new PropertiesManager(app.getAppUriName()));
        Mockito.when(propertiesManager.getControllerPort()).thenReturn(controllerPort);

        final ControlManager controlManager = new ControlManager(app.getAppUriName());

        final Field propertiesManagerField = ControlManager.class.getDeclaredField("propertiesManager");
        propertiesManagerField.setAccessible(true);
        propertiesManagerField.set(controlManager, propertiesManager);

        final Field controlManagerField = Service.class.getDeclaredField("controlManager");
        controlManagerField.setAccessible(true);
        controlManagerField.set(app, controlManager);

        return app;
    }

    public static synchronized int obtainPort() {
        return availablePort++;
    }


    @AfterClass
    public static void cleanUp() {
        Server.shutdownGracefully();
    }

    @Test
    public void helloWorldCorrectFunctionality() {

        HelloWorld h = new HelloWorld();
        String result = h.execute(new JSONObject());
        JSONObject jsonResult = new JSONObject(result);

        assertTrue(jsonResult.getString("msg").equals("Hello World") && jsonResult.getInt("statusCode") == 200);
    }

    @Test
    public void goodByeWorldCorrectFunctionality() {

        GoodByeWorld h = new GoodByeWorld();
        String result = h.execute(new JSONObject());

        assertEquals("{\"msg\":\"GoodBye World\"}", result);
    }

    @Test
    public void readControllerPort() {
        int port = new ExampleApp().getControlManager().getPropertiesManager().getControllerPort();
        assertTrue(port >= 4000 && port < 5000);
    }


    @Test
    public void freeze() {
        ExecutorService threadPool = Executors.newScheduledThreadPool(3);
        ExampleApp app = null;
        try {
            app = mockApp();
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }

        app.start();
        app.getControlManager().freeze();
        final Callable<String> getRequest = () -> HttpClient.get("api/example", "HELLO_WORLD");
        final int sleepSeconds = 4;
        Runnable resumeAfterXSeconds = app.getControlManager()::resume;
        Executors.newSingleThreadScheduledExecutor().schedule(resumeAfterXSeconds, sleepSeconds, TimeUnit.SECONDS);

        Future<String> getRequestFuture = threadPool.submit(getRequest);

        try {
            assertEquals(getRequestFuture.get(), "{\"msg\":\"Hello World\"}");
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }


    }

    @Test
    public void callingExampleAppAPI() {
        ExampleApp app = null;
        try {
            app = mockApp();
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }

        app.start();
        String response = null;
        try {
            response = HttpClient.get("api/example", "HELLO_WORLD");
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        assertEquals(response, "{\"msg\":\"Hello World\"}");
    }


    @Test
    public void deleteCommandWorksAsExpected() {
        ExampleApp app = null;
        try {
            app = mockApp();
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }

        app.start();
        app.getControlManager().getClassManager().deleteCommand("HELLO_WORLD");
        String response = null;
        try {
            response = HttpClient.get("api/example", "HELLO_WORLD");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals("{\"msg\":\"Function-Name class: (HELLO_WORLD) not found\"}", response);
    }

    @Test
    public void setMaxThreadsCountWaitsForActiveThreadsToTerminate() {
        ExampleApp app = null;
        try {
            app = mockApp();
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }

        app.start();
        final String testString = "Hello";
        Callable<String> callable = () -> {
            Thread.sleep(5 * 1000);
            return testString;
        };
        Future<String> future = null;
        try {
            future = getThreadPool(app).submit(callable);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e.getMessage());
        }
        app.getControlManager().setMaxThreadsCount(4);

        try {
            // This assert shows that the reloadThreadCount waits until the callable has finished ans submitted its value
            assertEquals(testString, future.get());
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void threadPoolReloadsProperlyAfterSettingMaxThreadsCount() {
        ExampleApp app = null;
        try {
            app = mockApp();
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }

        app.start();

        app.getControlManager().setMaxThreadsCount(4);
        final String testString = "Hello";
        Callable<String> trivialCallable = () -> testString;
        Future<String> future = null;
        try {
            future = getThreadPool(app).submit(trivialCallable);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e.getMessage());
        }
        try {
            assertEquals(testString, future.get());
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void startThenResumeThenFreeze() {
        ExampleApp app = null;
        try {
            app = mockApp();
        } catch (ReflectiveOperationException e) {
            fail(e.getMessage());
        }

        app.start();
        app.getControlManager().resume();
        app.getControlManager().freeze();
    }
}
