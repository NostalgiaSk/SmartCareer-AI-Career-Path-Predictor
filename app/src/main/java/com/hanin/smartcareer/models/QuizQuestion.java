package com.hanin.smartcareer.models;

import java.io.Serializable;
import java.util.ArrayList;


public class QuizQuestion implements Serializable {

    private String question;           // The question text
    private ArrayList<String> options; // Exactly 4 answer choices
    private int correctIndex;          // 0-based index of the correct answer

    

    public QuizQuestion() {}

 

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
