package com.printpass.job.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class CloudStorageService {

    @Autowired
    private AmazonS3 s3Client;

    @Value("${cloud.digitalocean.spaces.bucket}")
    private String bucketName;

    @Value("${cloud.digitalocean.spaces.endpoint}")
    private String endpoint;

    public String uploadFileToCloud(MultipartFile file) throws IOException {
        // Har file ke aage ek random ID lagayen taake naam clash na hon
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // File ki maloomat (metadata) tayyar karein
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // Cloud par file bhejne ki request (Sath mein file ko public read allow kar rahe hain taake download ho sake)
        PutObjectRequest request = new PutObjectRequest(bucketName, uniqueFileName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        // Boom! 🚀 File aasmaan ki taraf rawana
        s3Client.putObject(request);

        // Wapas aane wala URL banayen (https://printpass-storage.sgp1.digitaloceanspaces.com/file_name.pdf)
        return "https://" + bucketName + "." + endpoint + "/" + uniqueFileName;
    }

    // 🧹 Jadu: Cloud se file automatically delete karne wala method
    public void deleteFileFromCloud(String fileUrl) {
        try {
            // URL mein se asal file ka naam (key) nikalna
            // Misaal: URL ke aakhir mein jo "e9d2aef2..._FitPulseERDiagram.png" hai, usay alag karna
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            // DigitalOcean ko hukum dena ke is file ko hamesha ke liye uda do!
            s3Client.deleteObject(bucketName, fileName);
            System.out.println("✅ Cloud Safai Jadu: File [" + fileName + "] hamesha ke liye delete ho gayi!");

        } catch (Exception e) {
            System.err.println("❌ Error: Cloud se file delete nahi ho saki - " + e.getMessage());
        }
    }
}