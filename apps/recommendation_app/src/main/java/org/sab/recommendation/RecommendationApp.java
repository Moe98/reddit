package org.sab.recommendation;

import org.sab.service.Service;

public class RecommendationApp extends Service {
    @Override
    public String getAppUriName() {
        return "RECOMMENDATION";
    }

    @Override
    public int getThreadCount() {
        return 10;
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }

    public static void main(String[] args) {
        new RecommendationApp().start();
    }
}
