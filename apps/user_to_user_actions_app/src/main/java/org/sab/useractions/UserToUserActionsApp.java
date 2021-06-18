package org.sab.useractions;

import org.sab.service.Service;

public class UserToUserActionsApp extends Service {

    public static void main(String[] args) {
        new UserToUserActionsApp().start();
    }

    @Override
    public String getAppUriName() {
        return "useraction";
    }

}