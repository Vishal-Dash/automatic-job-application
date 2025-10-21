package com.jobapp.service;

import com.jobapp.dto.SearchResult;
import jakarta.annotation.PostConstruct;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmbeddingService {


    private final RestTemplate rest = new RestTemplate();
    private final String EMBED_URL = "http://localhost:8000/embed";


    private final List<Map<String,String>> jobs = new ArrayList<>();
    private final List<double[]> jobEmbeddings = new ArrayList<>();


    @PostConstruct
    public void init() {
        jobs.add(Map.of("title","Senior Java Developer","company","Acme","desc","Work on microservices, Spring Boot, cloud"));
        jobs.add(Map.of("title","Frontend Engineer","company","Beta Labs","desc","React, Typescript, UX, animations"));
        jobs.add(Map.of("title","ML Engineer","company","GammaAI","desc","PyTorch, transformers, model deployment"));


        for (Map<String,String> job : jobs) {
            double[] emb = embedding(job.get("title") + " - " + job.get("desc"));
            jobEmbeddings.add(emb);
        }
    }


    public double[] embedding(String text) {
        Map<String, Object> payload = Map.of("inputs", List.of(text));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> req = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<Map> resp = rest.exchange(EMBED_URL, HttpMethod.POST, req, Map.class);

            Map body = resp.getBody();
            if (body == null) return new double[0];
            List<List<Number>> embList = (List<List<Number>>) body.get("embeddings");
            if (embList == null || embList.isEmpty()) return new double[0];
            List<Number> e = embList.get(0);
            double[] arr = new double[e.size()];
            for (int i = 0; i < e.size(); i++) arr[i] = e.get(i).doubleValue();
            return arr;
        } catch (Exception ex) {
            // if the embedding backend is down or returns unexpected data, don't fail the app startup
            return new double[0];
        }
    }


    private double cosine(double[] a, double[] b) {
        RealVector va = new ArrayRealVector(a);
        RealVector vb = new ArrayRealVector(b);
        double denom = va.getNorm() * vb.getNorm();
        return denom == 0 ? 0 : va.dotProduct(vb) / denom;
    }


    public List<SearchResult> semanticSearch(String query, int k) {
        double[] qEmb = embedding(query);
        PriorityQueue<SearchResult> pq = new PriorityQueue<>(Comparator.comparingDouble(r -> r.score));
        for (int i = 0; i < jobs.size(); i++) {
            double score = cosine(qEmb, jobEmbeddings.get(i));
            Map<String, String> j = jobs.get(i);
            SearchResult r = new SearchResult(j.get("title"), j.get("company"), j.get("desc"), score);
            pq.add(r);
            if (pq.size() > k) pq.poll();
        }
        List<SearchResult> out = new ArrayList<>();
        while (!pq.isEmpty()) out.add(pq.poll());
        Collections.reverse(out);
        return out;
    }
}
