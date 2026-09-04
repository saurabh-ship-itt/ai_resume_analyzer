package com.example.resumeanalyzer.service;

import com.example.resumeanalyzer.entity.Candidate;
import com.example.resumeanalyzer.entity.CandidateSkill;
import com.example.resumeanalyzer.entity.Experience;
import com.example.resumeanalyzer.entity.Job;
import com.example.resumeanalyzer.repository.CandidateRepository;
import com.example.resumeanalyzer.repository.JobMatchRepository;
import com.example.resumeanalyzer.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class JobMatchingServiceTest {

    private CandidateRepository candidateRepository;
    private JobRepository jobRepository;
    private JobMatchRepository jobMatchRepository;
    private JobMatchingService service;

    @BeforeEach
    void setup() {
        candidateRepository = Mockito.mock(CandidateRepository.class);
        jobRepository = Mockito.mock(JobRepository.class);
        jobMatchRepository = Mockito.mock(JobMatchRepository.class);
        service = new JobMatchingService(candidateRepository, jobRepository, jobMatchRepository, 0.7, 0.3);
    }

    @Test
    void match_computesScoresCorrectly() {
        Candidate c = new Candidate();
        c.setId(1L);
        List<CandidateSkill> skills = new ArrayList<>();
        CandidateSkill cs = CandidateSkill.builder().skillName("Java").candidate(c).build();
        skills.add(cs);
        c.setSkills(skills);
        List<Experience> exps = new ArrayList<>();
        Experience e = Experience.builder().company("X").role("Developer").yearsOfExperience(2.0).candidate(c).build();
        exps.add(e);
        c.setExperiences(exps);

        Job j = Job.builder().id(10L).title("Java Dev").requiredSkills(List.of("Java","Spring Boot","SQL")).minimumExperience(1).build();

        when(candidateRepository.findById(1L)).thenReturn(Optional.of(c));
        when(jobRepository.findById(10L)).thenReturn(Optional.of(j));

        var resp = service.match(1L, 10L);
        assertEquals(1L, resp.getCandidateId());
        assertEquals(10L, resp.getJobId());
        // skillScore = 1/3 *100 = 33.33
        assertTrue(resp.getSkillMatchScore() > 30 && resp.getSkillMatchScore() < 35);
        // experienceScore should be 100 because candidateExp >= minExp
        assertEquals(100.0, resp.getExperienceScore());
        // overall = 33.33*0.7 + 100*0.3 ~ 53.33
        assertTrue(resp.getOverallScore() > 50 && resp.getOverallScore() < 60);
    }

}
