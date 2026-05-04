package com.elearning.resourceserver.infrastructure.minio;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private static final String UPLOADS_BUCKET = "elearning-uploads";
    private static final String MEDIA_BUCKET = "elearning-media";
    private static final String PUBLIC_BUCKET = "elearning-public";

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

    public String generatePresignedUrl(String bucketName, String objectName, int expiryMinutes) throws Exception {
        return minioClient.getPresignedObjectUrl(
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
