package org.sab.user;


import org.sab.service.Service;

public class UserApp extends Service
{
    public static void main( String[] args) {
        new UserApp().start();
    }

    @Override
    public String getAppUriName() {
        return "user";
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
