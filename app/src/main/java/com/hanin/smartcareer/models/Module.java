package com.hanin.smartcareer.models;


public class Module {

    private String moduleId;
    private String title;
    private boolean completed;


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
