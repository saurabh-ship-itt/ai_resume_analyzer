package com.example.resumeanalyzer.service;

import com.example.resumeanalyzer.dto.RecommendationDto;
import com.example.resumeanalyzer.entity.Job;
import com.example.resumeanalyzer.nlp.SkillDictionary;
import com.example.resumeanalyzer.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final JobRepository jobRepository;

    public SearchService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<RecommendationDto> searchJobs(String query, int topN) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        String normalized = SkillDictionary.normalize(query);
        String[] tokens = normalized.split(" ");

        List<Job> jobs = jobRepository.findAll();
        List<RecommendationDto> results = new ArrayList<>();

        for (Job job : jobs) {
            double score = scoreJob(job, tokens);
            results.add(RecommendationDto.builder()
                    .jobId(job.getId())
                    .title(job.getTitle())
                    .matchScore(Math.round(score * 100.0) / 100.0)
                    .build());
        }

        return results.stream().sorted(Comparator.comparing(RecommendationDto::getMatchScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    private double scoreJob(Job job, String[] tokens) {
        // simple heuristic: required skills matches = 0.6 weight, title/description tokens = 0.4
        double reqWeight = 0.6;
        double textWeight = 0.4;

        Set<String> reqNorm = job.getRequiredSkills() == null ? Collections.emptySet()
                : job.getRequiredSkills().stream().map(SkillDictionary::normalize).collect(Collectors.toSet());

        int reqMatches = 0;
        for (String t : tokens) {
            if (reqNorm.contains(t)) reqMatches++;
        }
        double reqScore = reqNorm.isEmpty() ? 0.0 : (reqMatches * 1.0 / reqNorm.size());

        String text = (job.getTitle() + " " + (job.getDescription() == null ? "" : job.getDescription())).toLowerCase();
        int textMatches = 0;
        for (String t : tokens) {
            if (t.length() > 0 && text.contains(t)) textMatches++;
        }
        double textScore = tokens.length == 0 ? 0.0 : (textMatches * 1.0 / tokens.length);

        return (reqScore * reqWeight + textScore * textWeight) * 100.0; // percentage
    }

}
