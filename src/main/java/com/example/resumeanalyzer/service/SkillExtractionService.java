package com.example.resumeanalyzer.service;

import com.example.resumeanalyzer.nlp.SkillDictionary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkillExtractionService {

    public List<String> extractSkills(String preprocessedText) {
        List<String> found = new ArrayList<>();
        if (preprocessedText == null || preprocessedText.isEmpty()) return found;

        String normalized = SkillDictionary.normalize(preprocessedText);

        // Check for multi-word and single-word skills by scanning normalized text
        for (String canonical : SkillDictionary.getAllCanonicalSkills()) {
            String normSkill = SkillDictionary.normalize(canonical);
            if (normSkill.isEmpty()) continue;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\b" + java.util.regex.Pattern.quote(normSkill) + "\\b");
            if (p.matcher(normalized).find()) {
                found.add(canonical);
            }
        }

        for (java.util.Map.Entry<String, String> entry : SkillDictionary.getAllNormalizedMappings().entrySet()) {
            String normalizedVariant = entry.getKey();
            if (normalizedVariant.isEmpty()) continue;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\b" + java.util.regex.Pattern.quote(normalizedVariant) + "\\b");
            if (p.matcher(normalized).find() && !found.contains(entry.getValue())) {
                found.add(entry.getValue());
            }
        }

        return found;
    }

}
