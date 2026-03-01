package com.printpass.job.controller;

import com.printpass.job.dto.JobRequest;
import com.printpass.job.dto.JobResponse;
import com.printpass.job.enums.JobStatus;
import com.printpass.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(@RequestBody JobRequest request) {
        return jobService.createJob(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<JobResponse> getAllJobs() {
        return jobService.getAllJobs();
    }

    // Nayi PUT API: Status Update karne ke liye
    @PutMapping("/{jobId}/status")
    @ResponseStatus(HttpStatus.OK)
    public JobResponse updateJobStatus(@PathVariable Long jobId, @RequestParam JobStatus status) {
        return jobService.updateJobStatus(jobId, status);
    }

    // Nayi GET API: Vendor isay hit karega
    @GetMapping("/verify/{pin}")
    @ResponseStatus(HttpStatus.OK)
    public JobResponse verifyPin(@PathVariable String pin) {
        return jobService.getJobByPin(pin);
    }
}