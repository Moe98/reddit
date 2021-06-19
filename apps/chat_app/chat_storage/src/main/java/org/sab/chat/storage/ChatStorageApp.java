package org.sab.chat.storage;

import org.sab.service.Service;

public class ChatStorageApp extends Service {

    @Override
    public String getAppUriName() {
        return "CHAT";
    }

    public static void main(String[] args){
        new ChatStorageApp().start();
    }
}
