package org.sab.chat.storage;

import org.sab.service.Service;

public class ChatStorageApp extends Service {

    @Override
    public String getAppUriName() {
        return "CHAT_STORAGE_APP";
    }

    @Override
    public int getThreadCount() {
        return 8;
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }

    public static void main(String[] args){
        new ChatStorageApp().start();
    }
}
