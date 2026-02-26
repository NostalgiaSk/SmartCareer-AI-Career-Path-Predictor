package com.hanin.smartcareer.models;

import com.google.firebase.Timestamp;


public class User {

    
    private String uid;
    private String email;
    private String fullName;
    private String role;
    private int careerProfilesCount;
    private Timestamp createdAt;

    // ── Constructors ─────────────────────────────────────────────────

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String uid, String email, String fullName, String role, int careerProfilesCount) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.careerProfilesCount = careerProfilesCount;

    }

    // ── Getters & Setters ────────────────────────────────────────────

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getCareerProfilesCount() {
        return careerProfilesCount;
    }

    public void setCareerProfilesCount(int careerProfilesCount) {
        this.careerProfilesCount = careerProfilesCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
