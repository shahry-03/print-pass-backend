package com.printpass.job.service;

import com.printpass.job.dto.request.UploadRequest; // baad mein banayenge
import com.printpass.job.dto.response.JobResponse;
import com.printpass.job.dto.response.PrintResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface JobService {
    // Student file upload karega → PIN milega
    JobResponse uploadFile(MultipartFile file);

    // Print shop PIN enter karega → file aayegi
    PrintResponse getFileByPin(String secretKey, UUID shopId);

    // Print shop print button dabayega → print trigger
    PrintResponse printJob(String secretKey, UUID shopId);
}