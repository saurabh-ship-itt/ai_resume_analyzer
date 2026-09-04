package com.example.resumeanalyzer.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InformationExtractionService {

    private static final String[] EDUCATION_KEYWORDS = new String[]{
            "b.tech", "b.e", "bachelor", "bsc", "b.s", "bca", "m.tech", "mca", "master", "msc", "mba", "m.s", "phd"
    };

    private static final String[] EXPERIENCE_KEYWORDS = new String[]{
            "software engineer", "developer", "intern", "consultant", "manager", "analyst", "sr", "senior"
    };

    public List<String> extractEducationStrings(String text) {
        List<String> results = new ArrayList<>();
        if (text == null) return results;
        String lower = text.toLowerCase();
        String[] lines = lower.split("\\n");
        Pattern yearPattern = Pattern.compile("(19|20)\\d{2}");

        for (String line : lines) {
            for (String key : EDUCATION_KEYWORDS) {
                if (line.contains(key)) {
                    String original = line.trim();
                    Matcher m = yearPattern.matcher(line);
                    String year = null;
                    if (m.find()) year = m.group();
                    if (year != null) original = original + " (" + year + ")";
                    results.add(original);
                    break;
                }
            }
        }
        return results;
    }

    public List<ExtractedExperience> extractExperienceEntries(String text) {
        List<ExtractedExperience> results = new ArrayList<>();
        if (text == null) return results;
        String lower = text.toLowerCase();
        String[] lines = lower.split("\\n");

        Pattern yearsPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s+years?");

        for (String line : lines) {
            for (String key : EXPERIENCE_KEYWORDS) {
                if (line.contains(key)) {
                    String original = line.trim();
                    Double yrs = null;
                    Matcher m = yearsPattern.matcher(line);
                    if (m.find()) {
                        try {
                            yrs = Double.parseDouble(m.group(1));
                        } catch (NumberFormatException ignored) {}
                    }
                    ExtractedExperience ee = new ExtractedExperience();
                    ee.role = original;
                    ee.yearsOfExperience = yrs == null ? 0.0 : yrs;
                    // company heuristic: look for 'at <company>'
                    Pattern atPattern = Pattern.compile("at\\s+([a-zA-Z0-9 &\\.\\-]+)");
                    Matcher atM = atPattern.matcher(line);
                    if (atM.find()) ee.company = atM.group(1).trim();
                    results.add(ee);
                    break;
                }
            }
        }
        return results;
    }

    public List<String> extractProjects(String text) {
        List<String> projects = new ArrayList<>();
        if (text == null) return projects;
        String[] lines = text.split("\\n");
        boolean inProjects = false;
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toLowerCase().startsWith("projects") || trimmed.toLowerCase().startsWith("project")) {
                inProjects = true;
                continue;
            }
            if (inProjects) {
                if (trimmed.isEmpty()) {
                    if (current.length() > 0) {
                        projects.add(current.toString().trim());
                        current.setLength(0);
                    }
                } else {
                    // stop if next section likely begins (all caps or contains ':')
                    if (trimmed.equals(trimmed.toUpperCase()) && trimmed.length() > 3) {
                        break;
                    }
                    if (trimmed.contains(":")) {
                        break;
                    }
                    current.append(trimmed).append(" ");
                }
            }
        }
        if (current.length() > 0) projects.add(current.toString().trim());
        return projects;
    }

    public static class ExtractedExperience {
        public String company;
        public String role;
        public Double yearsOfExperience;
    }

}
