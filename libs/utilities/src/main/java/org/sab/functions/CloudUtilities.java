package org.sab.functions;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CloudUtilities {
    private Cloudinary cloudinary;

    static final Map<String, String> CONFIG;

    static {
        CONFIG = new HashMap<>();
        CONFIG.put("cloud_name", System.getenv("COLOUDINARY_CLOUD_NAME"));
        CONFIG.put("api_key", System.getenv("COLOUDINARY_API_KEY"));
        CONFIG.put("api_secret", System.getenv("COLOUDINARY_API_SECRET"));
    }

    public CloudUtilities() throws EnvironmentVariableNotLoaded {
        for (Map.Entry<String, String> entry : CONFIG.entrySet())
            if (entry.getValue() == null)
                throw new EnvironmentVariableNotLoaded("COLOUDINARY_" + entry.getKey().toUpperCase());
        cloudinary = new Cloudinary(CONFIG);
    }

    public static String uploadImage(String photoUrl, String userId) throws IOException, EnvironmentVariableNotLoaded {
        String publicId = userId.replaceAll("[-]", "");
        Cloudinary cloudinary = new CloudUtilities().cloudinary;
        Map<String, String> uploadResult = cloudinary.uploader().upload(photoUrl, ObjectUtils.asMap("public_id", publicId));
        return cloudinary.url().generate(uploadResult.get("public_id"));
    }

    public static void deleteImage(String userId) throws IOException, EnvironmentVariableNotLoaded {
        String publicId = userId.replaceAll("[-]", "");
        Cloudinary cloudinary = new CloudUtilities().cloudinary;
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
    }
}
