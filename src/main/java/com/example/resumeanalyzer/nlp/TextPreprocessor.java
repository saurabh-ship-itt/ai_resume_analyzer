package com.example.resumeanalyzer.nlp;

import java.util.*;
import java.util.stream.Collectors;

public class TextPreprocessor {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a","an","the","and","or","but","if","then","else","when","at","by","for",
            "in","on","of","to","with","is","are","was","were","be","been","this","that",
            "these","those","as","from","we","he","she","they","it","its","I","you","your",
            "our","us","will","would","can","could","should","may","also","have","has","had"
    ));

    public static String preprocess(String text) {
        if (text == null) return "";
        // lowercase
        String s = text.toLowerCase(Locale.ROOT);
        // remove non-alphanumeric (keep spaces)
        s = s.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]+", " ");
        // normalize whitespace
        s = s.replaceAll("\\s+", " ").trim();

        // tokenize
        String[] tokens = s.split(" ");

        List<String> filtered = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String t : tokens) {
            if (t.isBlank()) continue;
            if (STOP_WORDS.contains(t)) continue;
            // dedupe while preserving order
            if (seen.add(t)) filtered.add(normalizeToken(t));
        }

        return String.join(" ", filtered);
    }

    public static List<String> tokenize(String text) {
        if (text == null) return Collections.emptyList();
        String s = text.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]+", " ")
                .replaceAll("\\s+", " ").trim();
        if (s.isEmpty()) return Collections.emptyList();
        return Arrays.stream(s.split(" "))
                .filter(tok -> !tok.isBlank())
                .map(TextPreprocessor::normalizeToken)
                .collect(Collectors.toList());
    }

    private static String normalizeToken(String t) {
        // simple normalization: map common variants
        if (t.equalsIgnoreCase("springboot")) return "spring boot";
        if (t.equalsIgnoreCase("javascript")) return "javascript"; // keep as-is
        return t;
    }

}
