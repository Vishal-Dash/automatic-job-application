package com.jobapp.model;

public class Job {
    private String title;
    private String company;
    private String location;
    private String link;
    private String description;
    private double similarity;

    public Job() {}

    public Job(String title, String company, String location, String link, String description, double similarity) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.link = link;
        this.similarity = similarity;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public double getSimilarity() { return similarity; }
    public void setSimilarity(double similarity) { this.similarity = similarity; }
}
