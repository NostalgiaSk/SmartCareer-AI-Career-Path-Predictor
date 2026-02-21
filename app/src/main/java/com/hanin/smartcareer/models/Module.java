package com.hanin.smartcareer.models;

/**
 * Model representing a single learning module within a certification.
 * Used by {@link com.hanin.smartcareer.adapters.ModuleAdapter} in the
 * Certification Detail screen.
 */
public class Module {

    private String moduleId;
    private String title;
    private boolean completed;

    /**
     * @param moduleId  Unique identifier (e.g. Firestore document ID).
     * @param title     Display title (e.g. "Module 1: Intro to ML").
     * @param completed Whether the student has completed this module.
     */
    public Module(String moduleId, String title, boolean completed) {
        this.moduleId  = moduleId;
        this.title     = title;
        this.completed = completed;
    }

    // ── Getters ──────────────────────────────────────────────────────

    public String getModuleId()  { return moduleId; }
    public String getTitle()     { return title; }
    public boolean isCompleted() { return completed; }

    // ── Setters ──────────────────────────────────────────────────────

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
