package com.example.resumeanalyzer.controller;

import com.example.resumeanalyzer.dto.CandidateResponseDto;
import com.example.resumeanalyzer.dto.CandidateSummaryDto;
import com.example.resumeanalyzer.entity.Candidate;
import com.example.resumeanalyzer.repository.CandidateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateRepository candidateRepository;

    public CandidateController(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @GetMapping
    @Operation(summary = "Get all candidates")
    public ResponseEntity<List<CandidateSummaryDto>> listCandidates() {
        List<CandidateSummaryDto> list = candidateRepository.findAll().stream()
                .map(c -> CandidateSummaryDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .email(c.getEmail())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get candidate by id")
    public ResponseEntity<CandidateResponseDto> getCandidate(@PathVariable Long id) {
        Candidate c = candidateRepository.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Candidate not found: " + id));
        CandidateResponseDto dto = CandidateResponseDto.builder()
                .candidateId(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .skills(c.getSkills().stream().map(s -> s.getSkillName()).collect(Collectors.toList()))
                .education(c.getEducation().stream().map(e -> e.getDegree()).collect(Collectors.toList()))
                .experience(c.getExperiences().stream().map(ex -> ex.getRole() + (ex.getYearsOfExperience()!=null?" ("+ex.getYearsOfExperience()+" yrs)":"")).collect(Collectors.toList()))
                .projects(java.util.Collections.emptyList())
                .build();
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete candidate by id")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        Candidate c = candidateRepository.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Candidate not found: " + id));
        candidateRepository.delete(c);
        return ResponseEntity.noContent().build();
    }

}
