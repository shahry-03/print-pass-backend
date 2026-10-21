package com.printpass.job.storage;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully!", bucketName);
            } else {
                log.info("Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            log.error("Could not check/create bucket: {}", e.getMessage());
        }
    }

    // ✅ File Upload karo → URL return karo
    public String uploadFile(MultipartFile file) {
        try {
            // Unique file naam banao — Duplicate se bachne ke liye
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // DigitalOcean Spaces mein upload karo
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded successfully: {}", uniqueFileName);

            // File ka URL return karo
            return uniqueFileName;

        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage());
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    // ✅ Presigned URL banao — Print ke waqt
    // Temporary URL jo sirf kuch time ke liye valid hoga
    public String getPresignedUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .expiry(1, TimeUnit.HOURS)  // 1 ghante ke liye valid
                            .build()
            );
        } catch (Exception e) {
            log.error("Presigned URL generation failed: {}", e.getMessage());
            throw new RuntimeException("Could not generate file URL: " + e.getMessage());
        }
    }

    // ✅ File delete karo — Job complete hone ke baad
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File deleted successfully: {}", fileName);
        } catch (Exception e) {
            log.error("File deletion failed: {}", e.getMessage());
            throw new RuntimeException("File deletion failed: " + e.getMessage());
        }
    }
}