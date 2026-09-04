package com.example.resumeanalyzer.controller;

import com.example.resumeanalyzer.dto.RecommendationDto;
import com.example.resumeanalyzer.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Search", description = "Job search and ranking")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search jobs by keywords and rank results")
    public ResponseEntity<List<RecommendationDto>> search(@RequestParam("query") String query,
                                                          @RequestParam(value = "topN", defaultValue = "10") int topN) {
        return ResponseEntity.ok(searchService.searchJobs(query, topN));
    }

}
