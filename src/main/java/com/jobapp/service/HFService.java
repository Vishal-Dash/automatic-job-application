package com.jobapp.service;

import com.jobapp.dto.SearchResult;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class HFService {
    private static final Logger logger = LoggerFactory.getLogger(HFService.class);
    private final RestTemplate rest = new RestTemplate();
    @Value("${hf.token:}")
    private String hfToken;
    // simple in-memory job corpus; in reality load from DB or sheet
    private final List<Map<String,String>> jobs = new ArrayList<>();
    private final List<double[]> jobEmbeddings = new ArrayList<>();
    @PostConstruct
    public void init() {
// sample job postings
        jobs.add(Map.of("title","Senior Java Developer","company","Acme","desc","Work on microservices, Spring Boot, cloud"));
        jobs.add(Map.of("title","Frontend Engineer","company","Beta Labs","desc","React, Typescript, UX, animations"));
        jobs.add(Map.of("title","ML Engineer","company","GammaAI","desc","PyTorch, transformers, model deployment"));
// precompute embeddings
        for (Map<String,String> job : jobs) {
            try {
                double[] emb = embedding(job.get("title") + " - " +
                        job.get("desc"));
                jobEmbeddings.add(emb == null ? new double[0] : emb);
            } catch (Exception ex) {
                // log and continue so bean creation doesn't fail
                logger.warn("Failed to compute embedding for job {}: {}", job.get("title"), ex.toString());
                jobEmbeddings.add(new double[0]);
            }
        }
    }
    private HttpHeaders makeHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (hfToken != null && !hfToken.isBlank()){
            headers.setBearerAuth(hfToken);
        }
        return headers;
    }
    // call HF embedding model
    public double[] embedding(String text){
        try {
            String url = "https://api-inference.huggingface.co/embeddings/sentence-transformers/all-MiniLM-L6-v2";
            Map<String,Object> body = Map.of("inputs", text);
            HttpEntity<Map<String,Object>> req = new HttpEntity<>(body,
                    makeHeaders());
            ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.POST, req,
                    Map.class);
    // response has {"embedding": [ ... ]}
            Map data = resp.getBody();
            if (data == null) return new double[0];
            Object embObj = data.get("embedding");
            if (!(embObj instanceof List)) return new double[0];
            List<Number> embList = (List<Number>) embObj;
            double[] arr = new double[embList.size()];
            for (int i=0;i<embList.size();i++) arr[i] =
                    embList.get(i).doubleValue();
            return arr;
        } catch (Exception e) {
            logger.warn("Error fetching embedding from HF API: {}", e.toString());
            return new double[0];
        }
    }
    private double cosine(double[] a, double[] b){
        RealVector va = new ArrayRealVector(a);
        RealVector vb = new ArrayRealVector(b);
        double denom = va.getNorm()*vb.getNorm();
        if (denom == 0) return 0;
        return va.dotProduct(vb)/denom;
    }
    public List<SearchResult> semanticSearch(String query, int k){
        double[] qEmb = embedding(query);
        PriorityQueue<SearchResult> pq = new
                PriorityQueue<>(Comparator.comparingDouble(r->r.score));
        for (int i=0;i<jobs.size();i++){
            double score = cosine(qEmb, jobEmbeddings.get(i));
            Map<String,String> j = jobs.get(i);
            SearchResult r = new SearchResult(j.get("title"),
                    j.get("company"), j.get("desc"), score);
            pq.add(r);
            if (pq.size()>k) pq.poll();
        }
        List<SearchResult> out = new ArrayList<>();
        while(!pq.isEmpty()) out.add(pq.poll());
        Collections.reverse(out);
        return out;
    }
    // optional: simple generation using flan-t5-small
    public String generateSummary(String prompt){
        String url = "https://api-inference.huggingface.co/models/google/flan-t5-small";
        Map<String,Object> body = Map.of("inputs", prompt, "parameters", Map.of("max_new_tokens", 60));
        HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, makeHeaders());
        ResponseEntity<List> resp = rest.exchange(url, HttpMethod.POST, req, List.class);
        List<Map<String,Object>> out = resp.getBody();
        if (out == null || out.isEmpty()) return "";
        Object text = out.get(0).get("generated_text");
        return text == null ? "" : text.toString();
    }
}