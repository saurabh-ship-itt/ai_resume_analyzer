package com.example.resumeanalyzer.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SkillExtractionServiceTest {

    private final SkillExtractionService service = new SkillExtractionService();

    @Test
    void extractSkills_detectsCanonicalSkills() {
        String text = "Experienced in Java, Springboot, SQL and Docker";
        List<String> skills = service.extractSkills(text);
        assertTrue(skills.contains("Java"));
        assertTrue(skills.contains("Spring Boot"));
        assertTrue(skills.contains("SQL"));
        assertTrue(skills.contains("Docker"));
    }

}
