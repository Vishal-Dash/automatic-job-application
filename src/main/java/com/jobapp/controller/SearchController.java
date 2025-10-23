package com.jobapp.controller;

import com.jobapp.model.Job;
import com.jobapp.service.JobService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api")
public class SearchController {

    private final JobService jobService;
    public SearchController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/search")
    public List<Job> search(@RequestParam String query) {
        return jobService.searchJobs(query);
    }
}