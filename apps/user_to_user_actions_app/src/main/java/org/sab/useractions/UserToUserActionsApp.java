package org.sab.useractions;

import org.sab.service.Service;

public class UserToUserActionsApp extends Service {

    // TODO get this from config file
    private static final String USER_TO_USER_ACTIONS_APP_QUEUE = "USER_TO_USER_ACTIONS_APP_REQ";

    public static void main(String[] args) {
        new UserToUserActionsApp().start();
    }

    @Override
    public String getAppUriName() {
        return "user-to-user-actions";
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