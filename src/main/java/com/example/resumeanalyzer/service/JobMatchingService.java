package com.example.resumeanalyzer.service;

import com.example.resumeanalyzer.dto.MatchResponseDto;
import com.example.resumeanalyzer.dto.RecommendationDto;
import com.example.resumeanalyzer.entity.Candidate;
import com.example.resumeanalyzer.entity.CandidateSkill;
import com.example.resumeanalyzer.entity.Job;
import com.example.resumeanalyzer.entity.JobMatch;
import com.example.resumeanalyzer.repository.CandidateRepository;
import com.example.resumeanalyzer.repository.JobMatchRepository;
import com.example.resumeanalyzer.repository.JobRepository;
import com.example.resumeanalyzer.nlp.SkillDictionary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final JobMatchRepository jobMatchRepository;

    private final double skillWeight;
    private final double experienceWeight;

    public JobMatchingService(CandidateRepository candidateRepository,
                              JobRepository jobRepository,
                              JobMatchRepository jobMatchRepository,
                              @Value("${matching.skill.weight:0.7}") double skillWeight,
                              @Value("${matching.experience.weight:0.3}") double experienceWeight) {
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.skillWeight = skillWeight;
        this.experienceWeight = experienceWeight;
    }

    public MatchResponseDto match(Long candidateId, Long jobId) {
        Candidate candidate = candidateRepository.findById(candidateId).orElseThrow(() -> new NoSuchElementException("Candidate not found: " + candidateId));
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new NoSuchElementException("Job not found: " + jobId));

        Set<String> candidateSkills = candidate.getSkills().stream()
                .map(CandidateSkill::getSkillName)
                .filter(Objects::nonNull)
                .map(s -> SkillDictionary.normalize(s))
                .collect(Collectors.toSet());

        List<String> required = job.getRequiredSkills() == null ? Collections.emptyList() : job.getRequiredSkills();
        Set<String> requiredNorm = required.stream().map(SkillDictionary::normalize).collect(Collectors.toSet());

        Set<String> matchedNorm = new HashSet<>();
        Set<String> missingNorm = new HashSet<>();

        for (String r : requiredNorm) {
            if (candidateSkills.contains(r)) matchedNorm.add(r);
            else missingNorm.add(r);
        }

        double skillScore = requiredNorm.isEmpty() ? 100.0 : (matchedNorm.size() * 100.0 / requiredNorm.size());

        // compute candidate experience sum
        double candidateExp = candidate.getExperiences().stream()
                .map(e -> e.getYearsOfExperience() == null ? 0.0 : e.getYearsOfExperience())
                .mapToDouble(Double::doubleValue).sum();

        double expScore;
        if (job.getMinimumExperience() == null || job.getMinimumExperience() <= 0) {
            expScore = 100.0;
        } else {
            double minExp = job.getMinimumExperience();
            expScore = Math.min(100.0, (candidateExp / minExp) * 100.0);
        }

        double overall = (skillScore * skillWeight) + (expScore * experienceWeight);

        // map normalized back to canonical where possible
        List<String> matchedSkills = matchedNorm.stream()
                .map(n -> SkillDictionary.canonicalFromNormalized(n).orElse(n))
                .collect(Collectors.toList());
        List<String> missingSkills = missingNorm.stream()
                .map(n -> SkillDictionary.canonicalFromNormalized(n).orElse(n))
                .collect(Collectors.toList());

        MatchResponseDto resp = MatchResponseDto.builder()
                .candidateId(candidateId)
                .jobId(jobId)
                .jobTitle(job.getTitle())
                .skillMatchScore(round(skillScore))
                .experienceScore(round(expScore))
                .overallScore(round(overall))
                .matchedSkills(matchedSkills)
                .missingSkills(missingSkills)
                .recommendation(recommendationLabel(overall))
                .build();

        // persist JobMatch
        JobMatch jm = JobMatch.builder()
                .candidate(candidate)
                .job(job)
                .skillMatchScore(resp.getSkillMatchScore())
                .experienceScore(resp.getExperienceScore())
                .overallScore(resp.getOverallScore())
                .matchedSkills(resp.getMatchedSkills())
                .missingSkills(resp.getMissingSkills())
                .build();
        jobMatchRepository.save(jm);

        return resp;
    }

    private String recommendationLabel(double overall) {
        if (overall >= 80) return "Highly Relevant";
        if (overall >= 60) return "Relevant";
        if (overall >= 40) return "Partially Relevant";
        return "Low Relevance";
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public List<RecommendationDto> recommendForCandidate(Long candidateId, int topN) {
        Candidate candidate = candidateRepository.findById(candidateId).orElseThrow(() -> new NoSuchElementException("Candidate not found: " + candidateId));
        List<Job> jobs = jobRepository.findAll();

        List<RecommendationDto> items = new ArrayList<>();
        for (Job job : jobs) {
            MatchResponseDto m = match(candidateId, job.getId());
            items.add(RecommendationDto.builder()
                    .jobId(job.getId())
                    .title(job.getTitle())
                    .matchScore(m.getOverallScore())
                    .build());
        }

        return items.stream()
                .sorted(Comparator.comparing(RecommendationDto::getMatchScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

}
