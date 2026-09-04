package com.example.resumeanalyzer.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "experience")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Candidate candidate;

    private String company;

    private String role;

    private Double yearsOfExperience;

}
