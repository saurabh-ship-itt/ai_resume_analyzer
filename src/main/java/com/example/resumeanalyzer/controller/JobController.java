package com.example.resumeanalyzer.controller;

import com.example.resumeanalyzer.dto.JobRequestDto;
import com.example.resumeanalyzer.dto.JobResponseDto;
import com.example.resumeanalyzer.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Validated
@Tag(name = "Jobs", description = "Job management APIs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @Operation(summary = "Create a job posting")
    public ResponseEntity<JobResponseDto> createJob(@Valid @RequestBody JobRequestDto dto) {
        JobResponseDto created = jobService.createJob(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all jobs")
    public ResponseEntity<List<JobResponseDto>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by id")
    public ResponseEntity<JobResponseDto> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete job by id")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

}
