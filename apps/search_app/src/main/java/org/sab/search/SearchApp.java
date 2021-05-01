package org.sab.search;

import org.sab.service.ConfigMap;
import org.sab.service.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SearchApp extends Service {
    private static final String SEARCH_QUEUE = "SEARCH_REQ";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConfigMap.instantiate();
        getThreadPool(10);
        listenOnQueue(SEARCH_QUEUE);
    }
}
