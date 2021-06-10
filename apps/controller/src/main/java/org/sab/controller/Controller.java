package org.sab.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Controller {

    private final Map<String, String> appMap;

    public Controller() {
        appMap = initAppMap();
    }

    private static Map<String, String> initAppMap() {
        //TODO
        return new HashMap<>();
    }

    public static void main(String[] args) throws Exception {
        final Controller controller = new Controller();
        controller.listenToConsole();
    }

    private void sendMessageToApp(String targetApp, String message) throws Exception {
        new ControllerClient("127.0.0.1", 8080, message).start();
    }

    // TODO Object here is a placeholder. Type to be later determined.
    private void pushFileToApp(String targetApp, Object file) {
        throw new UnsupportedOperationException();
    }

    public void listenToConsole() throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            if (sc.hasNext()) {
                String cmd = sc.nextLine();
                sendMessageToApp("", cmd);
            }
        }
    }
}
