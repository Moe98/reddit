package org.sab.service;

class ServiceConstants {

    private ServiceConstants () {
    }

    public static final String REQUEST_QUEUE_NAME_SUFFIX = "_REQ";
    public static final int MAX_THREAD_TIMEOUT = 4;

    public static final String THREADS_COUNT_PROPERTY_NAME = "threadsCount",
                                DB_CONNECTIONS_COUNT_PROPERTY_NAME = "dbConnectionsCount";

    public static final int DEFAULT_THREADS_COUNT = 10,
                            DEFAULT_DB_CONNECTIONS_COUNT = 10;
}
