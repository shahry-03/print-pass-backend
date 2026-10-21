package com.printpass.job.enums;

public enum JobStatus {
    PENDING,    // Order bhej diya gaya hai
    PROCESSING, // Dukan wala print nikal raha hai
    READY,      // 🌟 NAYA STATUS: Print nikal chuka hai, bas customer ka intezar hai
    COMPLETED,  // Print handover ho gaya aur file delete ho gayi
    CANCELLED   // Order cancel ho gaya
}