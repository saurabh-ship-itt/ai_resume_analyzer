package com.example.resumeanalyzer.nlp;

import java.util.*;

public class SkillDictionary {

    // Canonical skills list (expandable)
    private static final List<String> CANONICAL_SKILLS = Arrays.asList(
            "Java",
            "Python",
            "C++",
            "Spring Boot",
            "Spring",
            "SQL",
            "MySQL",
            "MongoDB",
            "Docker",
            "AWS",
            "Git",
            "GitHub",
            "REST API",
            "React",
            "JavaScript",
            "HTML",
            "CSS",
            "Machine Learning",
            "NLP",
            "TensorFlow",
            "Scikit-learn"
    );

    // Normalized forms for matching (lowercase, punctuation removed)
    private static final Map<String, String> NORMALIZED_TO_CANONICAL = new HashMap<>();

    static {
        for (String s : CANONICAL_SKILLS) {
            String norm = normalize(s);
            NORMALIZED_TO_CANONICAL.put(norm, s);
        }

        // common variants
        NORMALIZED_TO_CANONICAL.put(normalize("springboot"), "Spring Boot");
        NORMALIZED_TO_CANONICAL.put(normalize("restapi"), "REST API");
        NORMALIZED_TO_CANONICAL.put(normalize("rest api"), "REST API");
        NORMALIZED_TO_CANONICAL.put(normalize("js"), "JavaScript");
        NORMALIZED_TO_CANONICAL.put(normalize("machinelearning"), "Machine Learning");
    }

    public static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[^a-z0-9 ]+", "").trim();
    }

    public static Set<String> getAllCanonicalSkills() {
        return new LinkedHashSet<>(CANONICAL_SKILLS);
    }

    public static Map<String, String> getAllNormalizedMappings() {
        return new LinkedHashMap<>(NORMALIZED_TO_CANONICAL);
    }

    public static Optional<String> canonicalFromNormalized(String normalized) {
        return Optional.ofNullable(NORMALIZED_TO_CANONICAL.get(normalized));
    }

}
