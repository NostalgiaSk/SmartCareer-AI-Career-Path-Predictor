package com.hanin.smartcareer;

import java.io.Serializable;
import java.util.ArrayList;

public class Career implements Serializable {

    private String careerName;
    private int matchScore;
    private String description;
    private ArrayList<String> skillGaps;
    private ArrayList<String> certifications;

    /**
     * @param careerName     Display name of the career path (e.g. "AI Engineer").
     * @param matchScore     AI-computed match percentage (0 – 100).
     * @param description    Brief explanation of why this career fits the student.
     * @param skillGaps      Skills the student needs to acquire for this career.
     * @param certifications Recommended certifications to strengthen the profile.
     */
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
}
