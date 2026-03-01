package com.printpass.job.enums;

public enum JobStatus {
    PENDING,    // Order bhej diya gaya hai
    PROCESSING, // Dukan wala print nikal raha hai
    COMPLETED,  // Print ho gaya, collect kar lein
    CANCELLED   // Order cancel ho gaya
}