package org.sab.minio;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class FileSimulation {
    private static final String SAMPLE_IMAGE_NAME = "SampleImagee.png";
    private static final String SAMPLE_IMAGE_TYPE = "image/png";

    public static JSONObject generateImageJson() throws IOException {
        InputStream is = FileSimulation.class.getClassLoader().getResourceAsStream(SAMPLE_IMAGE_NAME);
        if (is != null) {
            String data = org.apache.commons.codec.binary.Base64.encodeBase64String(is.readAllBytes());
            return new JSONObject().put("data", data).put("type", SAMPLE_IMAGE_TYPE);
        } else
            return null;
    }
}
