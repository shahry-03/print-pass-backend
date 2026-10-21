package com.printpass.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling; // <--- YEH IMPORT MISSING THA

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling // <--- Scheduled task ke liye zaroori hai
public class JobServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
} 