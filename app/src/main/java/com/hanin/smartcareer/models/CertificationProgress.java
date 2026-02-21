package com.hanin.smartcareer.models;

/**
 * Model representing a certification the student is currently progressing through.
 * Used by the "Certifications In Progress" section on the Home dashboard.
 */
public class CertificationProgress {

    private String certName;
    private String provider;
    private int progressPercent; // 0 – 100

    /**
     * @param certName        Display name (e.g. "Google ML Engineer").
     * @param provider        Certification provider (e.g. "Google").
     * @param progressPercent Completion percentage (0 – 100).
     */
    public CertificationProgress(String certName, String provider, int progressPercent) {
        this.certName        = certName;
        this.provider        = provider;
        this.progressPercent = progressPercent;
    }

    public String getCertName()        { return certName; }
    public String getProvider()        { return provider; }
    public int getProgressPercent()    { return progressPercent; }
}
