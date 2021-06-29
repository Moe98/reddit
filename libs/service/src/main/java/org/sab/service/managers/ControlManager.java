package org.sab.service.managers;

import org.json.JSONObject;
import org.sab.databases.PoolDoesNotExistException;
import org.sab.reflection.ReflectionUtils;
import org.sab.service.ServiceConstants;
import org.sab.service.controllerbackdoor.BackdoorServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ControlManager {
    private final String appUriName;
    private final ThreadPoolManager threadPoolManager = new ThreadPoolManager();
    private DBPoolManager dbPoolManager;
    private final QueueManager queueManager;
    private final PropertiesManager propertiesManager;
    private final ClassManager classManager = new ClassManager();

    private final static String ARGS = "args";
    private final int DB_INIT_AWAIT_MINUTES = 1;

    private boolean isFrozen = true;

    public ControlManager(String appUriName) {
        this.appUriName = appUriName;
        queueManager = new QueueManager(appUriName, new InvocationManager(threadPoolManager, classManager));
        propertiesManager = new PropertiesManager(appUriName);
    }

    public void handleControllerMessage(JSONObject message) {
        System.out.printf("%s has received a message from the controller!\n%s\n", appUriName, message.toString());
        Method method = ReflectionUtils.getMethod(ControlManager.class, message.getString("command"));
        try {
            boolean hasArgs = message.has(ARGS);
            final Object[] arguments = message.optJSONArray(ARGS).toList().toArray();
            method.invoke(this, hasArgs ? arguments : null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void listenToController(int controllerPort) {
        new Thread(() -> {
            try {
                new BackdoorServer(controllerPort, this).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void start() {
        try {
            propertiesManager.loadProperties();
            classManager.init();
            dbPoolManager = new DBPoolManager(propertiesManager.getRequiredDbs());
            dbPoolManager.initDbClasses();

            listenToController(propertiesManager.getControllerPort());
            queueManager.initAcceptingNewRequests();
            System.out.println("Connection to queue initialized");
        } catch (IOException | ReflectiveOperationException | TimeoutException e) {
            e.printStackTrace();
            releaseResourcesAndExit();
        }

        resume();
    }

    public void freeze() {
        if (isFrozen) {
            return;
        }
        try {
            queueManager.stopAcceptingNewRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadPoolManager.releaseThreadPool();
        dbPoolManager.releaseDbPools();

        isFrozen = true;
    }

    public void resume() {
        if (!isFrozen) {
            return;
        }
        System.out.println("About to resume...");
        threadPoolManager.initThreadPool(propertiesManager.getThreadCount());
        System.out.println("Thread pool is ready.");

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> future = scheduler.schedule(dbPoolManager::initDbPool, 1, TimeUnit.MILLISECONDS);
        try {
            System.out.println("Awaiting DB initialization");
            future.get(DB_INIT_AWAIT_MINUTES, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            scheduler.shutdownNow();
            releaseResourcesAndExit();
        } finally {
            System.out.println("Shutting down scheduler");
            scheduler.shutdownNow();
        }

        System.out.println("DB pool is ready.");
        try {
            queueManager.startAcceptingNewRequests();
        } catch (IOException e) {
            e.printStackTrace();
            releaseResourcesAndExit();
        }
        System.out.println("Accepting requests from queue.");
        isFrozen = false;
    }

    public void setMaxThreadsCount(int maxThreadsCount) {
        propertiesManager.updateProperty(ServiceConstants.THREADS_COUNT_PROPERTY_NAME, String.valueOf(maxThreadsCount));
        try {
            reloadThreadPool();
        } catch (IOException e) {
            e.printStackTrace();
            releaseResourcesAndExit();
        }
    }

    public void setMaxDbConnectionCountForAll(int maxDBConnectionCount) {
        try {
            dbPoolManager.setMaxConnectionCountForAll(maxDBConnectionCount);
        } catch (PoolDoesNotExistException e) {
            e.printStackTrace();
        }
        try {
            reloadDBPool();
        } catch (IOException e) {
            e.printStackTrace();
            releaseResourcesAndExit();
        }
    }

    public void setMaxDbConnectionCount(String clientName, int maxDBConnectionCount) {
        try {
            dbPoolManager.setMaxDbConnectionCount(clientName, maxDBConnectionCount);
        } catch (PoolDoesNotExistException e) {
            e.printStackTrace();
        }
        try {
            reloadDBPool();
        } catch (IOException e) {
            e.printStackTrace();
            releaseResourcesAndExit();
        }
    }

    private void reloadThreadPool() throws IOException {
        queueManager.stopAcceptingNewRequests();
        threadPoolManager.releaseThreadPool();
        threadPoolManager.initThreadPool(propertiesManager.getThreadCount());
        queueManager.startAcceptingNewRequests();
    }

    private void reloadDBPool() throws IOException {
        queueManager.stopAcceptingNewRequests();
        threadPoolManager.releaseThreadPool();
        dbPoolManager.releaseDbPools();
        dbPoolManager.initDbPool();
        threadPoolManager.initThreadPool(propertiesManager.getThreadCount());
        queueManager.startAcceptingNewRequests();
    }

    public void addCommand(String functionName, String className, ArrayList<Integer> byteList) {
        final byte[] b = convertIntArrayListToByteArray(byteList);
        addCommandWithBytes(functionName, className, b);
    }

    public void addCommandWithBytes(String functionName, String className, byte[] b) {
        classManager.addCommand(functionName, className, b);
    }

    public void updateCommand(String functionName, String className, ArrayList<Integer> byteList) {
        final byte[] b = convertIntArrayListToByteArray(byteList);
        updateCommandWithBytes(functionName, className, b);
    }

    public void updateCommandWithBytes(String functionName, String className, byte[] b) {
        classManager.updateCommand(functionName, className, b);
    }

    public void deleteCommand(String functionName) {
        classManager.deleteCommand(functionName);
    }

    public void releaseResourcesAndExit() {
        // release resources and halt app
        queueManager.dispose();
        threadPoolManager.dispose();
        if(dbPoolManager != null) {
            dbPoolManager.dispose();
        }

        // crash app
        System.exit(-1);
    }

    public ThreadPoolManager getThreadPoolManager() {
        return threadPoolManager;
    }

    public DBPoolManager getDbPoolManager() {
        return dbPoolManager;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }

    public ClassManager getClassManager() {
        return classManager;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    private byte[] convertIntArrayListToByteArray(ArrayList<Integer> list) {
        final byte[] b = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            b[i] = (byte) ((int) list.get(i));
        }
        return b;
    }
}
