package org.sab.minio;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MinIO {
    private static final Map<String, String> CONFIG;

    static {
        CONFIG = new HashMap<>();
        CONFIG.put("MINIO_HOST", System.getenv("MINIO_HOST"));
        CONFIG.put("MINIO_ROOT_USER", System.getenv("MINIO_ROOT_USER"));
        CONFIG.put("MINIO_ROOT_PASSWORD", System.getenv("MINIO_ROOT_PASSWORD"));
    }

    private static MinIO instance = null;
    private final MinioClient MINIO_CLIENT;

    private MinIO() throws EnvironmentVariableNotLoaded {
        for (Map.Entry<String, String> entry : CONFIG.entrySet())
            if (entry.getValue() == null)
                throw new EnvironmentVariableNotLoaded(entry.getKey());

        MINIO_CLIENT = MinioClient.builder()
                .endpoint(CONFIG.get("MINIO_HOST"))
                .credentials(CONFIG.get("MINIO_ROOT_USER"), CONFIG.get("MINIO_ROOT_PASSWORD"))
                .build();
    }

    public static MinIO getInstance() throws EnvironmentVariableNotLoaded {
        if (instance == null) {
            instance = new MinIO();
        }
        return instance;
    }

    public static String uploadObject(String bucketName, String id, String data, String contentType) throws EnvironmentVariableNotLoaded {
        String url = "";
        try {
            MinioClient minioClient = MinIO.getInstance().MINIO_CLIENT;
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()))
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

            byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(data);
            InputStream stream = new ByteArrayInputStream(bytes);
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(id)
                            .stream(stream, -1, 10485760)
                            .contentType(contentType)
                            .build());
            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(id)
                            .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static boolean deleteObject(String bucketName, String id) throws EnvironmentVariableNotLoaded {
        boolean deleted = false;
        try {
            MinioClient minioClient = MinIO.getInstance().MINIO_CLIENT;
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(id).build());
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(id).build());
            deleted = true;
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            e.printStackTrace();
        }
        return deleted;
    }
}
