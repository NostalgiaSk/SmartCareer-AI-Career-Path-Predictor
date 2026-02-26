package com.hanin.smartcareer.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a single multiple-choice quiz question.
 *
 * <p>Each question has exactly 4 answer options and one correct answer
 * identified by its 0-based index.</p>
 *
 * <p>Implements {@link Serializable} so it can be bundled with the
 * parent {@link Module} when passing between Activities.</p>
 */
public class QuizQuestion implements Serializable {

    private String question;           // The question text
    private ArrayList<String> options; // Exactly 4 answer choices
    private int correctIndex;          // 0-based index of the correct answer

    // ── No-arg constructor (required for Gson) ─────────────────────

    public QuizQuestion() {}

    // ── Full constructor ────────────────────────────────────────────

    public QuizQuestion(String question, ArrayList<String> options, int correctIndex) {
        this.question     = question;
        this.options      = options;
        this.correctIndex = correctIndex;
    }

    // ── Getters ─────────────────────────────────────────────────────

    public String getQuestion()          { return question; }
    public ArrayList<String> getOptions() { return options; }
    public int getCorrectIndex()         { return correctIndex; }

    // ── Setters ─────────────────────────────────────────────────────

    public void setQuestion(String question)          { this.question = question; }
    public void setOptions(ArrayList<String> options)  { this.options = options; }
    public void setCorrectIndex(int correctIndex)     { this.correctIndex = correctIndex; }
}
