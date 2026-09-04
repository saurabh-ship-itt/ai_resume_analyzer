package com.example.resumeanalyzer.nlp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextPreprocessorTest {

    @Test
    void preprocess_removesPunctuationAndStopwords_andDedupes() {
        String input = "Java, Spring Boot! and the the python.";
        String out = TextPreprocessor.preprocess(input);
        assertTrue(out.contains("java"));
        assertTrue(out.contains("spring" ) || out.contains("spring boot"));
        assertTrue(out.contains("python"));
        // stop word 'and' and 'the' should be removed
        assertFalse(out.contains("and"));
        assertFalse(out.contains("the"));
    }

}
