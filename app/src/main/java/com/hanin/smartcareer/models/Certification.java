package com.hanin.smartcareer.models;


public class Certification {

    private String certName;
    private String provider;
    private String difficulty;


    public Certification(String certName, String provider, String difficulty) {
        this.certName   = certName;
        this.provider   = provider;
        this.difficulty = difficulty;
    }

    public String getCertName()   { return certName; }
    public String getProvider()   { return provider; }
    public String getDifficulty() { return difficulty; }
}
