package org.sab.functions;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CloudUtilities {
    private Cloudinary cloudinary;

    static Map<String, String> config;

    static {
        config = new HashMap();
        config.put("cloud_name", System.getenv("COLOUDINARY_CLOUD_NAME"));
        config.put("api_key", System.getenv("COLOUDINARY_API_KEY"));
        config.put("api_secret", System.getenv("COLOUDINARY_API_SECRET"));
    }

    public CloudUtilities() throws EnvironmentVariableNotLoaded {
        for (Map.Entry<String, String> entry : config.entrySet())
            if (entry.getValue() == null)
                throw new EnvironmentVariableNotLoaded("COLOUDINARY_" + entry.getKey().toUpperCase());
        cloudinary = new Cloudinary(config);
    }

    public static String uploadImage(String photoUrl, String username) throws IOException, EnvironmentVariableNotLoaded {
        String publicId = username.replaceAll("[-]", "");
        Cloudinary cloudinary = new CloudUtilities().cloudinary;
        System.out.println("Uploading Image!");
        Map uploadResult = cloudinary.uploader().upload(photoUrl, ObjectUtils.asMap("public_id", publicId));
        String url = cloudinary.url().generate((String) uploadResult.get("public_id"));
        return url;
    }

    public static void destroyImage(String username) throws IOException, EnvironmentVariableNotLoaded {
        String publicId = username.replaceAll("[-]", "");
        Cloudinary cloudinary = new CloudUtilities().cloudinary;
        Map deleteParams = ObjectUtils.asMap("invalidate", true);
        cloudinary.uploader().destroy(publicId, deleteParams);
    }
}
