package org.sab.recommendation;

import org.sab.service.ConfigMap;
import org.sab.service.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RecommendationApp extends Service {
    private static final String RECOMMENDATION_APP_QUEUE = "RECOMMENDATION_REQ";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConfigMap.instantiate();
        getThreadPool(10);
        listenOnQueue(RECOMMENDATION_APP_QUEUE);
    }
}
