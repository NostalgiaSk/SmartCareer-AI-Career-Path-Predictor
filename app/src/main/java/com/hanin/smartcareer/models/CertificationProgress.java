package com.hanin.smartcareer.models;

/**
 * Model representing a certification the student is currently progressing through.
 * Used by the "Certifications In Progress" section on the Home dashboard.
 */
public class CertificationProgress {

    private String certName;
    private String provider;
    private int progressPercent; // 0 – 100

    // No-arg constructor required by Firestore deserialization
    public CertificationProgress() {}

    public CertificationProgress(String certName, String provider, int progressPercent) {
        this.certName        = certName;
        this.provider        = provider;
        this.progressPercent = progressPercent;
    }

    public String getCertName()        { return certName; }
    public String getProvider()        { return provider; }
    public int getProgressPercent()    { return progressPercent; }

    public void setCertName(String certName)              { this.certName = certName; }
    public void setProvider(String provider)              { this.provider = provider; }
    public void setProgressPercent(int progressPercent)   { this.progressPercent = progressPercent; }
}
