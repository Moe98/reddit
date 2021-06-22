package org.sab.service.managers;

import org.json.JSONObject;
import org.sab.controller.Controller;
import org.sab.databases.PoolDoesNotExistException;
import org.sab.reflection.ReflectionUtils;
import org.sab.service.ServiceConstants;
import org.sab.service.controllerbackdoor.BackdoorServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ControlManager {
    private final String appUriName;
    private final ThreadPoolManager threadPoolManager = new ThreadPoolManager();
    private DBPoolManager dbPoolManager;
    private final QueueManager queueManager;
    private final PropertiesManager propertiesManager;
    private final ClassManager classManager = new ClassManager();

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
            boolean hasArgs = message.has(Controller.ARGS);
            method.invoke(this, hasArgs ? message.optJSONArray(Controller.ARGS).toList().toArray() : null);
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
        } catch (IOException | ReflectiveOperationException e) {
            releaseResourcesAndExit();
        }

        listenToController(propertiesManager.getControllerPort());
        queueManager.initAcceptingNewRequests();
        resume();
    }

    public void freeze() {
        if (isFrozen) {
            return;
        }

        queueManager.stopAcceptingNewRequests();
        threadPoolManager.releaseThreadPool();
        dbPoolManager.releaseDbPools();

        isFrozen = true;
    }

    public void resume() {
        if (!isFrozen) {
            return;
        }

        threadPoolManager.initThreadPool(propertiesManager.getThreadCount());
        dbPoolManager.initDbPool();
        queueManager.startAcceptingNewRequests();

        isFrozen = false;
    }

    public void setMaxThreadsCount(int maxThreadsCount) {
        propertiesManager.updateProperty(ServiceConstants.THREADS_COUNT_PROPERTY_NAME, String.valueOf(maxThreadsCount));
        reloadThreadPool();
    }

    public void setMaxDbConnectionCountForAll(int maxDBConnectionCount) {
        dbPoolManager.setMaxConnectionCountForAll(maxDBConnectionCount);
        reloadDBPool();
    }

    public void setMaxDbConnectionCount(String clientName, int maxDBConnectionCount) {
        try {
            dbPoolManager.setMaxDbConnectionCount(clientName, maxDBConnectionCount);
        } catch (PoolDoesNotExistException e) {
            e.printStackTrace();
        }
        reloadDBPool();
    }

    private void reloadThreadPool() {
        queueManager.stopAcceptingNewRequests();
        threadPoolManager.releaseThreadPool();
        threadPoolManager.initThreadPool(propertiesManager.getThreadCount());
        queueManager.startAcceptingNewRequests();
    }

    private void reloadDBPool() {
        queueManager.stopAcceptingNewRequests();
        threadPoolManager.releaseThreadPool();
        dbPoolManager.releaseDbPools();
        dbPoolManager.initDbPool();
        threadPoolManager.initThreadPool(propertiesManager.getThreadCount());
        queueManager.startAcceptingNewRequests();
    }

    public void addCommand(String functionName, String className, byte[] b) {
        classManager.addCommand(functionName, className, b);
    }

    public void updateCommand(String functionName, String className, byte[] b) {
        classManager.updateCommand(functionName, className, b);
    }

    public void deleteCommand(String functionName) {
        classManager.deleteCommand(functionName);
    }

    public void releaseResourcesAndExit() {
        // release resources and halt app
        queueManager.dispose();
        threadPoolManager.dispose();
        dbPoolManager.dispose();

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
}
