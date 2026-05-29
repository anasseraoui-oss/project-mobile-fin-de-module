package com.elearning.resourceserver.infrastructure.minio;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioClient publicMinioClient;
    private static final String UPLOADS_BUCKET = "elearning-uploads";
    private static final String MEDIA_BUCKET = "elearning-media";
    private static final String PUBLIC_BUCKET = "elearning-public";

    public MinioService(@Qualifier("minioClient") MinioClient minioClient,
            @Qualifier("publicMinioClient") MinioClient publicMinioClient) {
        this.minioClient = minioClient;
        this.publicMinioClient = publicMinioClient;
    }

    public String uploadFile(MultipartFile file, String bucketName) throws Exception {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return filename;
    }

    public void ensureBucket(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public void uploadBytes(byte[] bytes, String bucketName, String objectName, String contentType) throws Exception {
        ensureBucket(bucketName);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    public boolean objectExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public String generatePresignedUrl(String bucketName, String objectName, int expiryMinutes) throws Exception {
        return publicMinioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expiryMinutes, TimeUnit.MINUTES)
                        .build()
        );
    }

    public void moveObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject) throws Exception {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(targetBucket)
                        .object(targetObject)
                        .source(CopySource.builder().bucket(sourceBucket).object(sourceObject).build())
                        .build()
        );
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(sourceBucket)
                        .object(sourceObject)
                        .build()
        );
    }

    public void deleteObject(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }
}
