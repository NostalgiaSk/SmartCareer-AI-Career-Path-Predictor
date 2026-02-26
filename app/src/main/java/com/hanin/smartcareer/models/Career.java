package com.hanin.smartcareer.models;

import java.io.Serializable;
import java.util.ArrayList;


public class Career implements Serializable {

    private String suggestionId; // Firestore document ID — used as key for saved_careers
    private String careerName;
    private int matchScore;
    private String description;
    private ArrayList<String> skillGaps;
    private ArrayList<String> certifications;
    private boolean aiGenerated; // true if this suggestion came from the AI engine

    // No-arg constructor required by Firestore deserialization
    public Career() {}

    public Career(String careerName, int matchScore, String description,
                  ArrayList<String> skillGaps, ArrayList<String> certifications) {
        this.careerName     = careerName;
        this.matchScore     = matchScore;
        this.description    = description;
        this.skillGaps      = skillGaps;
        this.certifications = certifications;
        this.aiGenerated    = true; // default to true for AI-created careers
    }

    // ── Getters ──────────────────────────────────────────────────────

    public String getSuggestionId()             { return suggestionId; }
    public String getCareerName()              { return careerName; }
    public int getMatchScore()                 { return matchScore; }
    public String getDescription()             { return description; }
    public ArrayList<String> getSkillGaps()    { return skillGaps; }
    public ArrayList<String> getCertifications() { return certifications; }
    public boolean isAiGenerated()             { return aiGenerated; }

    // ── Setters (required by Firestore) ──────────────────────────────

    public void setSuggestionId(String suggestionId)          { this.suggestionId = suggestionId; }
    public void setCareerName(String careerName)              { this.careerName = careerName; }
    public void setMatchScore(int matchScore)                 { this.matchScore = matchScore; }
    public void setDescription(String description)            { this.description = description; }
    public void setSkillGaps(ArrayList<String> skillGaps)     { this.skillGaps = skillGaps; }
    public void setCertifications(ArrayList<String> certifications) { this.certifications = certifications; }
    public void setAiGenerated(boolean aiGenerated)           { this.aiGenerated = aiGenerated; }
}
