package com.printpass.job.repository;

import com.printpass.job.entity.PrintJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Yeh import lazmi karein

@Repository
public interface JobRepository extends JpaRepository<PrintJob, Long> {

    // Naya Method: PIN ke zariye order dhoondna
    Optional<PrintJob> findByPin(String pin);
}