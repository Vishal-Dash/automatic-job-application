package com.jobapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobapp.constants.LocationSpecificJobUrl;
import com.jobapp.model.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class JobService {

    @Value("${serpapi.key}")
    private String serpApiKey;

    // optional: number of results to request from SerpAPI; 0 means don't include the param
    // default to 100 results to avoid SerpAPI implicit 10-result limit; set serpapi.num in application.properties to override
    @Value("${serpapi.num:100}")
    private int serpNum;

    private final EmbeddingService embeddingService;
    private final ObjectMapper mapper = new ObjectMapper();

    public JobService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * Fetches job listings using SerpAPI's Google Jobs engine.
     */
    public List<Job> searchJobs(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(LocationSpecificJobUrl.BANGALORE_JOBS_URL, encodedQuery, serpApiKey);
            // if serpNum > 0, request that many results from SerpAPI (avoids implicit defaults)
            if (serpNum > 0) {
                // SerpAPI supports a num parameter for some endpoints; append it when configured
                url = url + "&num=" + serpNum;
            }
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            List<Job> jobs = new ArrayList<>();

            // Parse Google Jobs results
            if (root.has("jobs_results")) {
                for (JsonNode jobNode : root.get("jobs_results")) {
                    String title = jobNode.path("title").asText("");
                    String company = jobNode.path("company_name").asText("");
                    String location = jobNode.path("location").asText("");
                    String snippet = jobNode.path("description").asText("");
                    String link = jobNode.path("apply_options").isArray() && jobNode.get("apply_options").size() > 0
                            ? jobNode.get("apply_options").get(0).path("link").asText("")
                            : jobNode.path("link").asText("");

                    jobs.add(new Job(title, company, location, link, snippet, 0.0));
                }
            }

            // Compute embeddings for query and jobs
            double[] queryEmbedding = embeddingService.getEmbedding(query);

            for (Job job : jobs) {
                String textToEmbed = job.getTitle() + " " + job.getCompany() + " " +
                        job.getLocation() + " " + job.getDescription();
                double[] jobEmbedding = embeddingService.getEmbedding(textToEmbed);
                double similarity = embeddingService.cosineSimilarity(queryEmbedding, jobEmbedding);
                job.setSimilarity(similarity);
            }

            jobs.sort(Comparator.comparingDouble(Job::getSimilarity).reversed());
            return jobs;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}