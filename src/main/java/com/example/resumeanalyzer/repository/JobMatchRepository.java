package com.example.resumeanalyzer.repository;

import com.example.resumeanalyzer.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {
}
