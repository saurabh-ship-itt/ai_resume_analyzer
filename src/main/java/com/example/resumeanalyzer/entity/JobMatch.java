package com.example.resumeanalyzer.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "job_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Job job;

    private Double skillMatchScore;

    private Double experienceScore;

    private Double overallScore;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "jobmatch_matched_skills", joinColumns = @JoinColumn(name = "jobmatch_id"))
    @Column(name = "skill")
    private java.util.List<String> matchedSkills = new java.util.ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "jobmatch_missing_skills", joinColumns = @JoinColumn(name = "jobmatch_id"))
    @Column(name = "skill")
    private java.util.List<String> missingSkills = new java.util.ArrayList<>();

}
