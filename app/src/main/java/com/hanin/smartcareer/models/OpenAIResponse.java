package com.hanin.smartcareer.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Maps the JSON response from the OpenAI Chat Completion API.
 *
 * <h3>Relevant response structure</h3>
 * <pre>
 * {
 *   "choices": [
 *     {
 *       "message": {
 *         "role": "assistant",
 *         "content": "{ ... actual JSON string ... }"
 *       }
 *     }
 *   ]
 * }
 * </pre>
 *
 * The {@code content} field contains the raw JSON string produced by
 * the model, which should then be parsed into a list of {@link Career}
 * objects using Gson.
 */
public class OpenAIResponse {

    @SerializedName("choices")
    private List<Choice> choices;

    /**
     * Extracts the text content from the first choice.
     *
     * @return The raw JSON string from the assistant, or {@code null}
     *         if the response is empty.
     */
    public String extractContent() {
        if (choices != null && !choices.isEmpty()
                && choices.get(0).message != null) {
            return choices.get(0).message.content;
        }
        return null;
    }

    public List<Choice> getChoices() { return choices; }

    // ── Nested classes matching the API JSON ────────────────────────

    public static class Choice {

        @SerializedName("message")
        public MessageBody message;

        public static class MessageBody {

            @SerializedName("role")
            public String role;

            @SerializedName("content")
            public String content;
        }
    }

    /**
     * Wrapper for the top-level {@code "careers"} array inside the
     * assistant's JSON content. Used by Gson to deserialise the
     * model output into a list of {@link Career} objects.
     *
     * <pre>
     * CareerListWrapper wrapper = gson.fromJson(content, CareerListWrapper.class);
     * List&lt;Career&gt; careers = wrapper.getCareers();
     * </pre>
     */
    public static class CareerListWrapper {

        @SerializedName("careers")
        private List<CareerEntry> careers;

        public List<CareerEntry> getCareers() { return careers; }

        /**
         * Mirrors the JSON shape returned by the AI prompt.
         * Converted into the app's {@link Career} model downstream.
         */
        public static class CareerEntry {

            @SerializedName("name")
            public String name;

            @SerializedName("matchScore")
            public int matchScore;

            @SerializedName("description")
            public String description;

            @SerializedName("skillGaps")
            public List<String> skillGaps;

            @SerializedName("certifications")
            public List<String> certifications;
        }
    }
}
