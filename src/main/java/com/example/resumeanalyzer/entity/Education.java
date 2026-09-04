package com.example.resumeanalyzer.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "education")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Candidate candidate;

    private String degree;

    private String institution;

    private Integer graduationYear;

}
