package com.example.resumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponseDto {
    private Long candidateId;
    private Long jobId;
    private String jobTitle;
    private Double skillMatchScore;
    private Double experienceScore;
    private Double overallScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String recommendation;
}
