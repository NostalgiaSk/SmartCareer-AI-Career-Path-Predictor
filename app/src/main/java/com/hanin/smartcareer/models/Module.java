package com.hanin.smartcareer.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single learning module within a certification path.
 *
 * <p>Each module contains structured content, a duration estimate,
 * a sequential order index, and an embedded quiz. Modules follow
 * a locked → unlocked → completed lifecycle.</p>
 *
 * <p>Implements {@link Serializable} so it can be passed between
 * Activities via Intent extras.</p>
 */
public class Module implements Serializable {

    private String moduleId;       // Unique identifier (e.g. "mod_1")
    private String title;          // Display title (e.g. "Module 1: Intro to ML")
    private String content;        // Lesson body text
    private String duration;       // Estimated time (e.g. "25 min")
    private int orderIndex;        // Sequential position (0-based)
    private boolean completed;     // Whether the student finished this module
    private boolean locked;        // Whether the module is accessible
    private boolean quizPassed;    // Whether the quiz has been passed
    private int quizScore;         // Latest quiz score percentage (0 – 100)
    private ArrayList<QuizQuestion> quizQuestions; // Embedded quiz questions

    // ── No-arg constructor (required for Gson / Firestore) ──────────

    public Module() {}

    // ── Full constructor ────────────────────────────────────────────

    public Module(String moduleId, String title, String content,
                  String duration, int orderIndex,
                  ArrayList<QuizQuestion> quizQuestions) {
        this.moduleId      = moduleId;
        this.title         = title;
        this.content       = content;
        this.duration      = duration;
        this.orderIndex    = orderIndex;
        this.quizQuestions = quizQuestions;
        this.completed     = false;
        this.locked        = orderIndex != 0; // First module always unlocked
        this.quizPassed    = false;
        this.quizScore     = 0;
    }

    // ── Legacy constructor (backward compatibility) ─────────────────

    public Module(String moduleId, String title, boolean completed) {
        this.moduleId   = moduleId;
        this.title      = title;
        this.completed  = completed;
        this.locked     = false;
        this.quizPassed = false;
        this.quizScore  = 0;
    }

    // ── Getters ─────────────────────────────────────────────────────

    public String getModuleId()                    { return moduleId; }
    public String getTitle()                       { return title; }
    public String getContent()                     { return content; }
    public String getDuration()                    { return duration; }
    public int getOrderIndex()                     { return orderIndex; }
    public boolean isCompleted()                   { return completed; }
    public boolean isLocked()                      { return locked; }
    public boolean isQuizPassed()                  { return quizPassed; }
    public int getQuizScore()                      { return quizScore; }
    public ArrayList<QuizQuestion> getQuizQuestions() { return quizQuestions; }

    // ── Setters ─────────────────────────────────────────────────────

    public void setModuleId(String moduleId)       { this.moduleId = moduleId; }
    public void setTitle(String title)             { this.title = title; }
    public void setContent(String content)         { this.content = content; }
    public void setDuration(String duration)       { this.duration = duration; }
    public void setOrderIndex(int orderIndex)      { this.orderIndex = orderIndex; }
    public void setCompleted(boolean completed)    { this.completed = completed; }
    public void setLocked(boolean locked)          { this.locked = locked; }
    public void setQuizPassed(boolean quizPassed)  { this.quizPassed = quizPassed; }
    public void setQuizScore(int quizScore)        { this.quizScore = quizScore; }
    public void setQuizQuestions(ArrayList<QuizQuestion> quizQuestions) {
        this.quizQuestions = quizQuestions;
    }
}
