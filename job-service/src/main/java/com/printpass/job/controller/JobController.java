package com.printpass.job.controller;

import com.printpass.job.dto.response.JobResponse;
import com.printpass.job.dto.response.PrintResponse;
import com.printpass.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    // ✅ Public endpoint — Student file upload karega
    @PostMapping("/upload")
    public ResponseEntity<JobResponse> uploadFile(
            @RequestParam("file") MultipartFile file) {

        log.debug("File upload request: {}", file.getOriginalFilename());
        JobResponse response = jobService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // ✅ Public endpoint — Student purane PIN se naya generate karega
    @PostMapping("/reprint/{oldPin}")
    public ResponseEntity<JobResponse> reprintJob(
            @PathVariable("oldPin") String oldPin) {

        log.debug("Reprint request for old PIN: {}", oldPin);
        JobResponse response = jobService.reprintJob(oldPin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ JWT Protected — Print shop PIN enter karega, file aayegi
    @GetMapping("/{pin}")
    public ResponseEntity<PrintResponse> getFileByPin(
            @PathVariable("pin") String pin,
            @RequestAttribute("X-User-Id") String shopId) { // UUID se String kar diya

        log.debug("Get file by PIN: {}", pin);
        
        // String ko UUID mein convert kiya
        PrintResponse response = jobService.getFileByPin(pin, UUID.fromString(shopId));
        return ResponseEntity.ok(response);
    }

    // ✅ JWT Protected — Print shop print button dabayega
    @PostMapping("/{pin}/print")
    public ResponseEntity<PrintResponse> printJob(
            @PathVariable("pin") String pin,
            @RequestAttribute("X-User-Id") String shopId) { //  UUID se String kar diya

        log.debug("Print request for PIN: {}", pin);
        
        // String ko UUID mein convert kiya
        PrintResponse response = jobService.printJob(pin, UUID.fromString(shopId));
        return ResponseEntity.ok(response);
    }
}