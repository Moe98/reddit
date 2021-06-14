package org.sab.controller;

import org.json.JSONObject;
import org.sab.io.IoUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class Controller {

    private static final String instanceNumberDelimiter = "_";
    private static final String ENCODED_FILE = "encodedFile";
    private final Map<String, String> ipMap = new HashMap<>();
    private final Map<String, String> portMap = new HashMap<>();

    private Controller() {
        initMap(ipMap, "apps-ips.properties");
        initMap(portMap, "apps-ports.properties");
    }

    public static void main(String[] args) throws Exception {
        final Controller controller = new Controller();
        JSONObject request = new JSONObject(controller.readRequest());
        String command = request.getString("command");
        Method method = controller.getClass().getDeclaredMethod(command, JSONObject.class);
        method.invoke(controller, request);
    }

    private void initMap(Map<String, String> map, String configFile) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(configFile);
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (final String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));

        }
    }

    /**
     * @param appName String representing name of the app with or without id (e.g user/chat/user_1/chat_1)
     * @return list of ips for all instances of the app or empty list if invalid instance
     */
    public List<String> getIps(String appName) {

        if (ipMap.containsKey(appName))
            return List.of(ipMap.get(appName));
        String appInstancePattern = appName + instanceNumberDelimiter + "\\d+";
        List<String> ips = new ArrayList<>();
        for (Map.Entry<String, String> entry : ipMap.entrySet())
            if (Pattern.matches(appInstancePattern, entry.getKey()))
                ips.add(entry.getValue());

        return ips;
    }

    private int getPort(String instanceName) {
        if (portMap.containsKey(instanceName))
            return Integer.parseInt(portMap.get(instanceName));
        String appName = instanceName.substring(0, instanceName.lastIndexOf(instanceNumberDelimiter));
        return Integer.parseInt(portMap.get(appName));

    }


    private String readRequest() throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("request.json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder jsonString = new StringBuilder();
        while (bufferedReader.ready()) {
            jsonString.append(bufferedReader.readLine());
        }
        bufferedReader.close();
        return jsonString.toString();
    }

    private void sendMessageToApp(String message, String instanceName) throws Exception {
        sendMessage(getIps(instanceName), getPort(instanceName), message);
    }

    private void sendMessage(JSONObject request) throws Exception {
        sendMessageToApp(request.toString(), request.getString("app"));
    }

    private void sendMessage(String ip, int port, String message) throws Exception {
        new ControllerClient(ip, port, message).start();
    }

    private void sendMessage(List<String> ips, int port, String message) throws Exception {
        for (String ip : ips)
            sendMessage(ip, port, message);
    }

    private String fileNameToEncodedString(String fileName) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        return IoUtils.encodeFile(stream);
    }


    private void addOrUpdateClass(JSONObject request) throws Exception {
        String fileName = request.getString("fileName");
        request.put(ENCODED_FILE, fileNameToEncodedString(fileName));
        sendMessage(request);
    }

    /**
     * remote apps administration
     * called using reflection
     */
    public void setMaxThreadsCount(JSONObject request) throws Exception {
        sendMessage(request);
    }

    private void setMaxDbConnectionsCount(JSONObject request) throws Exception {
        sendMessage(request);
    }


    private void addCommand(JSONObject request) throws Exception {
        addOrUpdateClass(request);
    }

    private void deleteCommand(JSONObject request) throws Exception {
        sendMessage(request);
    }

    private void updateCommand(JSONObject request) throws Exception {
        addOrUpdateClass(request);
    }

    private void updateClass(JSONObject request) throws Exception {
        addOrUpdateClass(request);

    }

    private void freeze(JSONObject request) throws Exception {
        sendMessage(request);
    }

    private void resume(JSONObject request) throws Exception {
        sendMessage(request);
    }


}
