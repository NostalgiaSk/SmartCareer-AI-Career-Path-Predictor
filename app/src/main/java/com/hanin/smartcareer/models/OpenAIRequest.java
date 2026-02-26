package com.hanin.smartcareer.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OpenAIRequest {


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

    // ── Factory method: Career suggestions ─────────────────────────

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
        return new OpenAIRequest("llama-3.3-70b-versatile", messages, 0.4, 1024);
    }

    /**
     * Builds an {@link OpenAIRequest} that asks the AI to generate
     * a structured set of learning modules for a specific certification.
     *
     * <p>Each module includes a lesson body, estimated duration, and
     * 3 embedded quiz questions (4 options each).</p>
     *
     * @param certName  Name of the certification (e.g. "Google ML Engineer").
     * @param provider  Issuing organisation (e.g. "Google Cloud").
     * @param level     Difficulty level: "Beginner", "Intermediate", or "Advanced".
     * @return A ready-to-send request object.
     */
    public static OpenAIRequest buildModulesPrompt(String certName,
                                                    String provider,
                                                    String level) {

        String systemPrompt =
                "You are an expert education curriculum designer.\n\n" +
                "Given a professional certification, generate EXACTLY 5 structured learning modules " +
                "that a student should complete to prepare for this certification.\n\n" +
                "Return strictly valid JSON only.\n" +
                "Do NOT include explanations, comments, markdown formatting, or text outside JSON.\n\n" +
                "The JSON format must be:\n" +
                "{\n" +
                "  \"modules\": [\n" +
                "    {\n" +
                "      \"id\": \"mod_1\",\n" +
                "      \"title\": \"Module 1: Descriptive Module Title\",\n" +
                "      \"content\": \"A detailed 3-5 paragraph lesson explaining this topic. " +
                "Include key concepts, real-world applications, and practical examples.\",\n" +
                "      \"duration\": \"25 min\",\n" +
                "      \"quiz\": [\n" +
                "        {\n" +
                "          \"question\": \"Clear quiz question about the module content?\",\n" +
                "          \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
                "          \"correctIndex\": 0\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Requirements:\n" +
                "- Return EXACTLY 5 modules.\n" +
                "- Module ids must be sequential: mod_1, mod_2, mod_3, mod_4, mod_5.\n" +
                "- Each title must start with 'Module N:' followed by a concise, descriptive name.\n" +
                "- Content must be 3-5 informative paragraphs per module.\n" +
                "- Duration must be realistic (e.g. '20 min', '30 min', '45 min').\n" +
                "- Each module must have EXACTLY 3 quiz questions.\n" +
                "- Each quiz question must have EXACTLY 4 options.\n" +
                "- correctIndex must be a valid 0-based index (0, 1, 2, or 3).\n" +
                "- Quiz questions must directly test the module content.\n" +
                "- Order modules from foundational to advanced topics.\n" +
                "- The last module should be a capstone/assessment module.\n" +
                "- Return only JSON.";

        String userPrompt =
                "Certification Details:\n" +
                "- Name: " + certName + "\n" +
                "- Provider: " + provider + "\n" +
                "- Level: " + level;

        List<Message> messages = new ArrayList<>(Arrays.asList(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        ));

        // Increased max_tokens to 4096 to accommodate full content + quiz data
        return new OpenAIRequest("llama-3.3-70b-versatile", messages, 0.4, 4096);
    }

    // ── Getters (used by Gson serialisation) ─────────────────────────

    public String getModel()          { return model; }
    public List<Message> getMessages() { return messages; }
    public double getTemperature()    { return temperature; }
    public int getMaxTokens()         { return maxTokens; }
}
