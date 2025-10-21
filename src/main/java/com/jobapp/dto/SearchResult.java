package com.jobapp.dto;

public class SearchResult {
    public String title;
    public String company;
    public String snippet;
    public double score;


    public SearchResult(String title, String company, String snippet, double score) {
        this.title = title;
        this.company = company;
        this.snippet = snippet;
        this.score = score;
    }
}