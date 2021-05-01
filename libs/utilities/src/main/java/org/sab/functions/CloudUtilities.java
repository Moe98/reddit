package org.sab.functions;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CloudUtilities {
    private Cloudinary cloudinary;

    public CloudUtilities() {
        Map config = new HashMap();
        config.put("cloud_name", System.getenv("cloud_name"));
        config.put("api_key", System.getenv("api_key"));
        config.put("api_secret", System.getenv("api_secret"));
        cloudinary = new Cloudinary(config);
    }
    public static String uploadImage(String photoUrl,String username) throws IOException {
        String publicId = username.replaceAll("[-]", "");
        Cloudinary cloudinary = new CloudUtilities().cloudinary;
        System.out.println("Uploading Image!");
        Map uploadResult = cloudinary.uploader().upload(photoUrl, ObjectUtils.asMap("public_id", publicId));
        String url = cloudinary.url().generate((String) uploadResult.get("public_id"));
        return url;
    }
    public static void destroyImage(String username) throws IOException {
        String publicId = username.replaceAll("[-]", "");
        Cloudinary cloudinary = new CloudUtilities().cloudinary;
        Map deleteParams = ObjectUtils.asMap("invalidate", true );
        Map uploadResult = cloudinary.uploader().destroy(publicId,deleteParams);
    }
}
