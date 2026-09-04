package com.example.resumeanalyzer.service;

import com.example.resumeanalyzer.dto.CandidateResponseDto;
import com.example.resumeanalyzer.entity.Candidate;
import com.example.resumeanalyzer.entity.CandidateSkill;
import com.example.resumeanalyzer.repository.CandidateRepository;
import com.example.resumeanalyzer.repository.CandidateSkillRepository;
import com.example.resumeanalyzer.repository.EducationRepository;
import com.example.resumeanalyzer.repository.ExperienceRepository;
import com.example.resumeanalyzer.service.SkillExtractionService;
import com.example.resumeanalyzer.service.InformationExtractionService;
import com.example.resumeanalyzer.util.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.resumeanalyzer.nlp.TextPreprocessor;

@Service
public class ResumeParserService {

    private final CandidateRepository candidateRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final EducationRepository educationRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillExtractionService skillExtractionService;
    private final InformationExtractionService informationExtractionService;

    public ResumeParserService(CandidateRepository candidateRepository,
                               CandidateSkillRepository candidateSkillRepository,
                               EducationRepository educationRepository,
                               ExperienceRepository experienceRepository,
                               SkillExtractionService skillExtractionService,
                               InformationExtractionService informationExtractionService) {
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.skillExtractionService = skillExtractionService;
        this.informationExtractionService = informationExtractionService;
    }

    @Transactional
    public CandidateResponseDto parseAndSave(MultipartFile file) throws IOException, TikaException {
        FileUtils.validateFile(file);

        String text = null;
        try {
            Tika tika = new Tika();
            text = tika.parseToString(file.getInputStream());
        } catch (Throwable t) {
            // Fallback 1: UTF-8 string read
            try {
                text = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }

        if (text == null || text.trim().isEmpty()) {
            // Fallback 2: Extract printable ASCII characters from file bytes
            try {
                byte[] bytes = file.getBytes();
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    if ((b >= 32 && b <= 126) || b == 10 || b == 13 || b == 9) {
                        sb.append((char) b);
                    }
                }
                text = sb.toString();
            } catch (Exception ignored) {}
        }

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Extracted resume text is empty");
        }

        // preserve original for heuristics that depend on capitalization/structure
        String original = text;
        String cleaned = TextPreprocessor.preprocess(text);

        String email = extractEmail(original);
        String phone = extractPhone(original);
        String name = extractName(original);

        Candidate candidate = Candidate.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .resumeFileName(file.getOriginalFilename())
                .resumeText(cleaned)
                .skills(new java.util.ArrayList<>())
                .education(new java.util.ArrayList<>())
                .experiences(new java.util.ArrayList<>())
                .build();

        candidate = candidateRepository.save(candidate);
        if (candidate.getSkills() == null) candidate.setSkills(new java.util.ArrayList<>());
        if (candidate.getEducation() == null) candidate.setEducation(new java.util.ArrayList<>());
        if (candidate.getExperiences() == null) candidate.setExperiences(new java.util.ArrayList<>());

        // extract skills using preprocessed text
        java.util.List<String> skills = skillExtractionService.extractSkills(cleaned);
        for (String s : skills) {
            com.example.resumeanalyzer.entity.CandidateSkill cs = com.example.resumeanalyzer.entity.CandidateSkill.builder()
                .candidate(candidate)
                .skillName(s)
                .build();
            candidateSkillRepository.save(cs);
            candidate.getSkills().add(cs);
        }
        candidate = candidateRepository.save(candidate);

        // extract education
        java.util.List<String> educations = informationExtractionService.extractEducationStrings(original);
        for (String ed : educations) {
            com.example.resumeanalyzer.entity.Education e = com.example.resumeanalyzer.entity.Education.builder()
                .candidate(candidate)
                .degree(ed)
                .institution(null)
                .graduationYear(null)
                .build();
            educationRepository.save(e);
            candidate.getEducation().add(e);
        }

        // extract experience
        java.util.List<InformationExtractionService.ExtractedExperience> experiences = informationExtractionService.extractExperienceEntries(original);
        java.util.List<String> experienceSummaries = new java.util.ArrayList<>();
        for (InformationExtractionService.ExtractedExperience ee : experiences) {
            com.example.resumeanalyzer.entity.Experience ex = com.example.resumeanalyzer.entity.Experience.builder()
                .candidate(candidate)
                .company(ee.company)
                .role(ee.role)
                .yearsOfExperience(ee.yearsOfExperience)
                .build();
            experienceRepository.save(ex);
            candidate.getExperiences().add(ex);
            experienceSummaries.add((ee.role == null ? "" : ee.role) + (ee.yearsOfExperience != null ? (" ("+ee.yearsOfExperience+" years)") : ""));
        }
        candidate = candidateRepository.save(candidate);

        // extract projects (not persisted as entity)
        java.util.List<String> projects = informationExtractionService.extractProjects(original);

        return CandidateResponseDto.builder()
            .candidateId(candidate.getId())
            .name(candidate.getName())
            .email(candidate.getEmail())
            .phone(candidate.getPhone())
            .skills(skills)
            .education(educations)
            .experience(experienceSummaries)
            .projects(projects)
            .build();
    }

    private String extractEmail(String text) {
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher m = emailPattern.matcher(text);
        if (m.find()) return m.group();
        return null;
    }

    private String extractPhone(String text) {
        // Very simple phone extractor: finds 10+ digit sequences
        Pattern phonePattern = Pattern.compile("(\\+?\\d[\\d\\-(). ]{7,}\\d)");
        Matcher m = phonePattern.matcher(text);
        if (m.find()) {
            return m.group().replaceAll("[^0-9+]", "");
        }
        return null;
    }

    private String extractName(String text) {
        // naive heuristic: first non-empty line with 2-4 words and capitalized
        String[] lines = text.split("\\n");
        for (int i = 0; i < Math.min(lines.length, 8); i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] words = line.split("\\s+");
            if (words.length >= 2 && words.length <= 4) {
                // simple check: most words start with uppercase letter
                int cap = 0;
                for (String w : words) {
                    if (w.length() > 0 && Character.isUpperCase(w.charAt(0))) cap++;
                }
                if (cap >= Math.max(1, words.length - 1)) {
                    return line;
                }
            }
        }
        return null;
    }

}
