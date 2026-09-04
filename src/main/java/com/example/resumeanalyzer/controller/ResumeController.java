package com.example.resumeanalyzer.controller;

import com.example.resumeanalyzer.dto.CandidateResponseDto;
import com.example.resumeanalyzer.service.ResumeParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@Tag(name = "Resumes", description = "Resume upload and parsing APIs")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);
    private final ResumeParserService resumeParserService;

    public ResumeController(ResumeParserService resumeParserService) {
        this.resumeParserService = resumeParserService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a resume (PDF/DOCX/TXT) and parse candidate info")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            CandidateResponseDto dto = resumeParserService.parseAndSave(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid file upload: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("message", ex.getMessage())
            );
        } catch (Throwable ex) {
            log.error("Failed to process resume file: {}", file.getOriginalFilename(), ex);
            String errorMsg = ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : ex.getClass().getSimpleName();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to process resume: " + errorMsg)
            );
        }
    }

}
