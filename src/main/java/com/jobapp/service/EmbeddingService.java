package com.jobapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmbeddingService {

    private static final String EMBEDDING_API_URL = "http://localhost:8000/embed";
    private final ObjectMapper mapper = new ObjectMapper();

    public double[] getEmbedding(String text) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(EMBEDDING_API_URL);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity("{\"text\": \"" + text.replace("\"", "") + "\"}"));

            var response = client.execute(post);
            JsonNode json = mapper.readTree(response.getEntity().getContent());
            JsonNode arr = json.get("embedding");

            double[] embedding = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                embedding[i] = arr.get(i).asDouble();
            }
            return embedding;
        } catch (IOException e) {
            e.printStackTrace();
            return new double[0];
        }
    }

    public double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}