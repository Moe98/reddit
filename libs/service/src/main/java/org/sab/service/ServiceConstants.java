package org.sab.service;

import java.util.ArrayList;

public class ServiceConstants {

    private ServiceConstants () {
    }

    public static final String COMMAND_MAP_FILENAME = "commandMap.properties".toLowerCase();

    public static final String REQUEST_QUEUE_NAME_SUFFIX = "_REQ";
    public static final int MAX_THREAD_TIMEOUT = 4;

    public static final String THREADS_COUNT_PROPERTY_NAME = "threadsCount";
    public static final int DEFAULT_THREADS_COUNT = 10;

    public static final String REQUIRED_DATABASES_PROPERTY_NAME = "requiredDatabases";
    public static final String REQUIRED_DATABASES_ARRAY_DELIMITER = ",";
    public static final String REQUIRED_DATABASES_PAIR_DELIMITER = "-";
    public static final ArrayList<String> DEFAULT_REQUIRED_DATABASES = new ArrayList<>();

    public static final int DEFAULT_CONNECTION_COUNT = 10;

    public static final String GET_DB_CLIENT_METHOD_NAME = "getInstance";

}
