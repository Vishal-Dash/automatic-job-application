package com.jobapp.controller;

import com.jobapp.dto.SearchRequest;
import com.jobapp.dto.SearchResult;
import com.jobapp.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api")
public class SearchController {

    private final EmbeddingService embeddingService;
//    private final HFService hfService;
//    @PostMapping("/search")
//    public List<SearchResult> search(@RequestBody SearchRequest req){
//        int k = Math.max(1, Math.min(10, req.k()));
//        List<SearchResult> results = hfService.semanticSearch(req.query(), k);
//// optional: enrich with generated summaries (comment out if rate-limit)
//        for (SearchResult r: results){
//// r.snippet = hfService.generateSummary(r.title + " " + r.snippet);
//        }
//        return results;
//    }

    public SearchController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/search")
    public List<SearchResult> search(@RequestBody SearchRequest req) {
        int k = Math.max(1, Math.min(10, req.k()));
        return embeddingService.semanticSearch(req.query(), k);
    }
}