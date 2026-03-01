package com.printpass.job.repository;

import com.printpass.job.entity.PrintJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<PrintJob, Long> {
}