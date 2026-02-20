package com.hanin.smartcareer.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Request body for the OpenAI Chat Completion API.
 *
 * <p>Maps to the JSON structure expected by
 * {@code POST https://api.openai.com/v1/chat/completions}.
 *
 * <h3>Usage</h3>
 * <pre>
 * OpenAIRequest req = OpenAIRequest.buildCareerPrompt(
 *     "Java, Python", "AI, Data", 3.8, "ML chatbot project"
 * );
 * </pre>
 */
public class OpenAIRequest {

    // ── JSON fields ─────────────────────────────────────────────────

    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("temperature")
    private double temperature;

    @SerializedName("max_tokens")
    private int maxTokens;

    // ── Constructor ─────────────────────────────────────────────────

    public OpenAIRequest(String model, List<Message> messages,
                         double temperature, int maxTokens) {
        this.model       = model;
        this.messages    = messages;
        this.temperature = temperature;
        this.maxTokens   = maxTokens;
    }

    // ── Inner class: a single chat message ──────────────────────────

    public static class Message {

        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public Message(String role, String content) {
            this.role    = role;
            this.content = content;
        }

        public String getRole()    { return role; }
        public String getContent() { return content; }
    }

    // ── Factory method ──────────────────────────────────────────────

    /**
     * Builds a complete {@link OpenAIRequest} containing a system prompt
     * and a user prompt derived from the student's career profile.
     *
     * <p>The system message instructs the model to return <strong>strictly
     * valid JSON</strong> matching the shape expected by the app.
     *
     * @param skills    Comma-separated skills (e.g. "Java, Python, SQL").
     * @param interests Comma-separated interests (e.g. "AI, Data Science").
     * @param gpa       Student GPA on a 0.0 – 4.0 scale.
     * @param projects  Free-text description of academic projects.
     * @return A ready-to-send request object.
     */
    public static OpenAIRequest buildCareerPrompt(String skills,
                                                   String interests,
                                                   double gpa,
                                                   String projects) {

        // ── System prompt ────────────────────────────────────────────
        // Constrains the model to strict JSON with exact requirements.
        String systemPrompt =
                "I am building an Android app called SmartCareer – AI Career Path Predictor.\n\n" +
                "Based on the following student profile, " +
                "generate EXACTLY 5 career suggestions, ranked from highest match score to lowest.\n\n" +
                "Return strictly valid JSON only.\n" +
                "Do NOT include explanations, comments, markdown formatting, or text outside JSON.\n\n" +
                "The JSON format must be:\n" +
                "{\n" +
                "  \"careers\": [\n" +
                "    {\n" +
                "      \"name\": \"Career Name\",\n" +
                "      \"matchScore\": 0-100,\n" +
                "      \"description\": \"Professional and concise description (max 2 sentences)\",\n" +
                "      \"skillGaps\": [\"Missing Skill 1\", \"Missing Skill 2\"],\n" +
                "      \"certifications\": [\"Recommended Certification 1\", \"Recommended Certification 2\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Requirements:\n" +
                "- Return EXACTLY 5 different careers.\n" +
                "- matchScore must be an integer between 0 and 100.\n" +
                "- skillGaps must contain at least 2 items.\n" +
                "- certifications must contain at least 2 items.\n" +
                "- Do not repeat careers.\n" +
                "- Ensure output is valid JSON and parsable.\n" +
                "- Return only JSON.";

        // ── User prompt ──────────────────────────────────────────────
        // Injects the student's actual profile data.
        String userPrompt =
                "Student Profile:\n" +
                "- Skills: " + skills + "\n" +
                "- Interests: " + interests + "\n" +
                "- GPA: " + gpa + "\n" +
                "- Projects: " + projects;

        // Assemble message list (system first, then user)
        List<Message> messages = new ArrayList<>(Arrays.asList(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        ));

        // Use Groq's free LLaMA model — fast inference, no billing required.
        // Alternative Groq models: "mixtral-8x7b-32768", "gemma2-9b-it"
        return new OpenAIRequest("llama-3.3-70b-versatile", messages, 0.4, 1024);
    }

    // ── Getters (used by Gson serialisation) ─────────────────────────

    public String getModel()          { return model; }
    public List<Message> getMessages() { return messages; }
    public double getTemperature()    { return temperature; }
    public int getMaxTokens()         { return maxTokens; }
}
