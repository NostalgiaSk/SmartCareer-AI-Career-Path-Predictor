package com.hanin.smartcareer.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Data model representing a single AI-generated career suggestion.
 * Implements Serializable so it can be passed between activities via Intent extras.
 */
public class Career implements Serializable {

    private String careerName;
    private int matchScore;
    private String description;
    private ArrayList<String> skillGaps;
    private ArrayList<String> certifications;

    // No-arg constructor required by Firestore deserialization
    public Career() {}

    public Career(String careerName, int matchScore, String description,
                  ArrayList<String> skillGaps, ArrayList<String> certifications) {
        this.careerName     = careerName;
        this.matchScore     = matchScore;
        this.description    = description;
        this.skillGaps      = skillGaps;
        this.certifications = certifications;
    }

    // ── Getters ──────────────────────────────────────────────────────

    public String getCareerName()              { return careerName; }
    public int getMatchScore()                 { return matchScore; }
    public String getDescription()             { return description; }
    public ArrayList<String> getSkillGaps()    { return skillGaps; }
    public ArrayList<String> getCertifications() { return certifications; }

    // ── Setters (required by Firestore) ──────────────────────────────

    public void setCareerName(String careerName)              { this.careerName = careerName; }
    public void setMatchScore(int matchScore)                 { this.matchScore = matchScore; }
    public void setDescription(String description)            { this.description = description; }
    public void setSkillGaps(ArrayList<String> skillGaps)     { this.skillGaps = skillGaps; }
    public void setCertifications(ArrayList<String> certifications) { this.certifications = certifications; }
}
