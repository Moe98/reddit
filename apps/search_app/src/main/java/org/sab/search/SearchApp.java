package org.sab.search;

import org.sab.service.Service;

public class SearchApp extends Service {
    public static void main(String[] args) {
        new SearchApp().start();
    }

    @Override
    public String getAppUriName() {
        return "SEARCH";
    }

    @Override
    public int getThreadCount() {
        return 10;
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }
}
