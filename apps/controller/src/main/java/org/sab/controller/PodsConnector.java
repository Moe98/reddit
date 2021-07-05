package org.sab.controller;


import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.util.Config;

import java.io.IOException;

public class PodsConnector {

    private static String getPodIp(String podName) throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
        for(V1Pod pod: list.getItems()) {
            if(pod.getMetadata().getName().equals(podName))
                return pod.getStatus().getPodIP();
        }
        return null;

    }

    public static void main(String[] args) throws IOException, ApiException {

        System.out.println(getPodIp("postgres-deployment-d98d5ffd7-zr74l"));
    }
}