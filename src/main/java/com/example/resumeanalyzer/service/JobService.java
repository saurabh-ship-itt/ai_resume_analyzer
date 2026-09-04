package com.example.resumeanalyzer.service;

import com.example.resumeanalyzer.dto.JobRequestDto;
import com.example.resumeanalyzer.dto.JobResponseDto;
import com.example.resumeanalyzer.entity.Job;
import com.example.resumeanalyzer.exception.JobNotFoundException;
import com.example.resumeanalyzer.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public JobResponseDto createJob(JobRequestDto dto) {
        Job j = Job.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .requiredSkills(dto.getRequiredSkills())
                .minimumExperience(dto.getMinimumExperience())
                .build();
        j = jobRepository.save(j);
        return toDto(j);
    }

    public List<JobResponseDto> getAllJobs() {
        return jobRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public JobResponseDto getJobById(Long id) {
        Job j = jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
        return toDto(j);
    }

    public void deleteJob(Long id) {
        Job j = jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
        jobRepository.delete(j);
    }

    private JobResponseDto toDto(Job j) {
        return JobResponseDto.builder()
                .id(j.getId())
                .title(j.getTitle())
                .description(j.getDescription())
                .requiredSkills(j.getRequiredSkills())
                .minimumExperience(j.getMinimumExperience())
                .createdAt(j.getCreatedAt())
                .build();
    }

}
