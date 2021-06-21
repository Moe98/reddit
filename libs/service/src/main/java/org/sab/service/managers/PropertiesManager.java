package org.sab.service.managers;

import org.sab.service.ServiceConstants;
import org.sab.service.databases.DBConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PropertiesManager {

    private final Properties configProperties;
    private final String appUriName;
    
    public PropertiesManager(String appUriName) {
        this.appUriName = appUriName;
        configProperties = new Properties();
    }

    public void loadProperties() throws IOException {
        loadConfigProperties();
    }

    public void updateProperty(String propertyName, String newValue) {
        configProperties.replace(propertyName, newValue);
    }

    public int getThreadCount() {
        return readIntProperty(ServiceConstants.THREADS_COUNT_PROPERTY_NAME, ServiceConstants.DEFAULT_THREADS_COUNT);
    }

    public void loadConfigProperties() throws IOException {
        // TODO don't hardcode file name
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
        configProperties.load(inputStream);
    }

    private int readIntProperty(String propertyName, int defaultPropertyValue) {
        if (!configProperties.containsKey(propertyName))
            return defaultPropertyValue;
        return Integer.parseInt(configProperties.getProperty(propertyName));
    }

    private ArrayList<String> readArrayProperty(String propertyName, String delimiter, ArrayList<String> defaultPropertyValue) {
        if (!configProperties.containsKey(propertyName))
            return defaultPropertyValue;
        return new ArrayList<String>(Arrays.asList(configProperties.getProperty(propertyName).split(delimiter)));
    }

    public Map<String, DBConfig> getRequiredDbs() {
        final Map<String, DBConfig> requiredDbs = new HashMap<>();
        ArrayList<String> requiredDBsList = readArrayProperty(ServiceConstants.REQUIRED_DATABASES_PROPERTY_NAME,
                    ServiceConstants.REQUIRED_DATABASES_ARRAY_DELIMITER,
                    ServiceConstants.DEFAULT_REQUIRED_DATABASES);

        for(String dbPair : requiredDBsList) {
            String [] split = dbPair.split(ServiceConstants.REQUIRED_DATABASES_PAIR_DELIMITER);
            String classPath = split[0];
            int connectionCount = Integer.parseInt(split[1]);
            requiredDbs.put(classPath, new DBConfig(connectionCount));
        }

        System.out.println("Required Dbs: "  + requiredDbs);
        return requiredDbs;
    }

    public int getControllerPort() {
        final InputStream stream = getClass().getClassLoader().getResourceAsStream("apps-ports.properties");
        final Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String propertyName = appUriName.toLowerCase();
        return Integer.parseInt(properties.getProperty(propertyName));

    }
    
}