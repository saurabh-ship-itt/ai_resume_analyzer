package com.example.resumeanalyzer.controller;

import com.example.resumeanalyzer.dto.MatchResponseDto;
import com.example.resumeanalyzer.dto.RecommendationDto;
import com.example.resumeanalyzer.service.JobMatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Matching", description = "Job-candidate matching and recommendations")
public class MatchController {

    private final JobMatchingService jobMatchingService;

    public MatchController(JobMatchingService jobMatchingService) {
        this.jobMatchingService = jobMatchingService;
    }

    @GetMapping("/jobs/{jobId}/match/{candidateId}")
    @Operation(summary = "Get match score for a candidate against a job")
    public ResponseEntity<MatchResponseDto> match(@PathVariable Long jobId, @PathVariable Long candidateId) {
        MatchResponseDto resp = jobMatchingService.match(candidateId, jobId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/candidates/{candidateId}/recommendations")
    @Operation(summary = "Get top 5 job recommendations for a candidate")
    public ResponseEntity<List<RecommendationDto>> recommendations(@PathVariable Long candidateId) {
        List<RecommendationDto> recs = jobMatchingService.recommendForCandidate(candidateId,5);
        return ResponseEntity.ok(recs);
    }

}
