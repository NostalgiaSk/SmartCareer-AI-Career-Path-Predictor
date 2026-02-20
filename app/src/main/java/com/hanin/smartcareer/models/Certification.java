package com.hanin.smartcareer.models;

/**
 * Represents a recommended certification for a career path.
 */
public class Certification {

    private String certName;
    private String provider;
    private String difficulty;

    /**
     * @param certName   Name of the certification (e.g. "Google ML Engineer").
     * @param provider   Issuing organisation (e.g. "Google").
     * @param difficulty Human-readable level: "Beginner", "Intermediate", or "Advanced".
     */
    public Certification(String certName, String provider, String difficulty) {
        this.certName   = certName;
        this.provider   = provider;
        this.difficulty = difficulty;
    }

    public String getCertName()   { return certName; }
    public String getProvider()   { return provider; }
    public String getDifficulty() { return difficulty; }
}
