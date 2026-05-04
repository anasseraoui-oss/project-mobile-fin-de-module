// src/main/java/com/elearning/resourceserver/application/services/MinioService.java
package com.elearning.resourceserver.application.services;

import com.elearning.resourceserver.exceptions.MinioOperationException;
import com.elearning.resourceserver.exceptions.ValidationException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.public-url:http://localhost:9000}")
    private String minioPublicUrl;

    public String uploadFile(MultipartFile file, String bucket, String keyPrefix) {
        try {
            String filename = keyPrefix + UUID.randomUUID() + "_" + file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(filename)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return filename;
        } catch (Exception e) {
            log.error("Erreur lors de l'upload vers MinIO", e);
            throw new MinioOperationException("Impossible d'uploader le fichier", e);
        }
    }

    public String generatePresignedUrl(String bucket, String objectKey, int expiryMinutes) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
            // Remplace l'URL interne par l'URL publique si nécessaire
            if (url.contains("minio:9000")) {
                url = url.replace("http://minio:9000", minioPublicUrl);
            }
            return url;
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'URL signée", e);
            throw new MinioOperationException("Impossible de générer le lien temporaire", e);
        }
    }

    public void moveObject(String sourceBucket, String destBucket, String objectKey) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(destBucket)
                            .object(objectKey)
                            .source(CopySource.builder().bucket(sourceBucket).object(objectKey).build())
                            .build()
            );
            deleteObject(sourceBucket, objectKey);
        } catch (Exception e) {
            log.error("Erreur lors du déplacement de l'objet MinIO", e);
            throw new MinioOperationException("Impossible de déplacer le fichier", e);
        }
    }

    public void deleteObject(String bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'objet MinIO", e);
            throw new MinioOperationException("Impossible de supprimer le fichier", e);
        }
    }

    public String validateAndMoveVideoUpload(String tempKey) {
        if (!tempKey.endsWith(".mp4") && !tempKey.endsWith(".webm") && !tempKey.endsWith(".mov")) {
            throw new ValidationException("Format vidéo non supporté. Utilisez MP4, WEBM ou MOV.");
        }
        // Move from uploads to media
        moveObject("elearning-uploads", "elearning-media", tempKey);
        return tempKey;
    }
}
