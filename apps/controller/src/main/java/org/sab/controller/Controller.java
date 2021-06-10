package org.sab.controller;

import java.util.Map;

public class Controller {

    private final Map<String, String> appMap;

    public Controller() {
        appMap = initAppMap();
    }

    private static Map<String, String> initAppMap() {
        throw new UnsupportedOperationException();
    }

    private void sendMessageToApp(String targetApp, Map<String, String> message) {
        throw new UnsupportedOperationException();
    }

    // Object here is a placeholder. Type to be later determined.
    private void pushFileToApp(String targetApp, Object file) {
        throw new UnsupportedOperationException();
    }

    public void listenToConsole() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        final Controller controller = new Controller();
        controller.listenToConsole();
    }
}
