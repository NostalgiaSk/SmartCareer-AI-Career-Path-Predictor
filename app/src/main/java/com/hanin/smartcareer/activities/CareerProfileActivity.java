package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Career;
import com.hanin.smartcareer.models.OpenAIRequest;
import com.hanin.smartcareer.models.OpenAIResponse;
import com.hanin.smartcareer.network.OpenAIService;
import com.hanin.smartcareer.network.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Collects the student's career profile (skills, interests, GPA,
 * projects) and sends it to the OpenAI API via Retrofit.
 *
 * <h3>Flow</h3>
 * <ol>
 *   <li>User fills in the form and taps "Generate Career Suggestions".</li>
 *   <li>{@link #attemptSubmitProfile()} validates all fields.</li>
 *   <li>If valid, {@link #callOpenAI(String, String, double, String)}
 *       sends a POST to OpenAI and receives structured JSON.</li>
 *   <li>On success the JSON is parsed into {@link Career} objects and
 *       passed to {@link CareerSuggestionsActivity} via Intent.</li>
 *   <li>On failure a Toast shows the error message.</li>
 * </ol>
 */
public class CareerProfileActivity extends AppCompatActivity {

    private static final String TAG = "CareerProfileActivity";

    // ── View references ─────────────────────────────────────────────

    private TextInputLayout tilSkills;
    private TextInputLayout tilInterests;
    private TextInputLayout tilGpa;
    private TextInputLayout tilProjects;
    private TextInputEditText etSkills;
    private TextInputEditText etInterests;
    private TextInputEditText etGpa;
    private TextInputEditText etProjects;
    private Button btnGenerateSuggestions;

    // ── Lifecycle ───────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career_profile);
        initViews();
        setupListeners();
    }

    // ── View initialisation ─────────────────────────────────────────

    private void initViews() {
        tilSkills     = findViewById(R.id.tilSkills);
        tilInterests  = findViewById(R.id.tilInterests);
        tilGpa        = findViewById(R.id.tilGpa);
        tilProjects   = findViewById(R.id.tilProjects);

        etSkills      = findViewById(R.id.etSkills);
        etInterests   = findViewById(R.id.etInterests);
        etGpa         = findViewById(R.id.etGpa);
        etProjects    = findViewById(R.id.etProjects);

        btnGenerateSuggestions = findViewById(R.id.btnGenerateSuggestions);
    }

    private void setupListeners() {
        btnGenerateSuggestions.setOnClickListener(v -> attemptSubmitProfile());
    }

    // ── Validation ──────────────────────────────────────────────────

    private void attemptSubmitProfile() {

        // Read current input values (trim whitespace)
        String skills    = etSkills.getText()    != null ? etSkills.getText().toString().trim()    : "";
        String interests = etInterests.getText() != null ? etInterests.getText().toString().trim() : "";
        String gpaStr    = etGpa.getText()       != null ? etGpa.getText().toString().trim()       : "";
        String projects  = etProjects.getText()  != null ? etProjects.getText().toString().trim()  : "";

        // Reset any previous error messages
        tilSkills.setError(null);
        tilInterests.setError(null);
        tilGpa.setError(null);
        tilProjects.setError(null);

        boolean isValid = true;

        // 1. Skills validation
        if (skills.isEmpty()) {
            tilSkills.setError(getString(R.string.error_skills_empty));
            isValid = false;
        }

        // 2. Interests validation
        if (interests.isEmpty()) {
            tilInterests.setError(getString(R.string.error_interests_empty));
            isValid = false;
        }

        // 3. GPA validation
        double gpa = -1;
        if (gpaStr.isEmpty()) {
            tilGpa.setError(getString(R.string.error_gpa_empty));
            isValid = false;
        } else {
            try {
                gpa = Double.parseDouble(gpaStr);
                if (gpa < 0 || gpa > 4.0) {
                    tilGpa.setError(getString(R.string.error_gpa_range));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilGpa.setError(getString(R.string.error_gpa_range));
                isValid = false;
            }
        }

        // 4. Projects validation
        if (projects.isEmpty()) {
            tilProjects.setError(getString(R.string.error_projects_empty));
            isValid = false;
        }

        // Proceed if all checks pass
        if (isValid) {
            Toast.makeText(this, R.string.toast_profile_saved, Toast.LENGTH_SHORT).show();

            // Call the OpenAI API to generate career suggestions
            callOpenAI(skills, interests, gpa, projects);

            // Firestore placeholder (for future backend persistence)
            saveProfileToFirestore(skills, interests, gpa, projects);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // OPENAI API CALL
    // ══════════════════════════════════════════════════════════════════

    /**
     * Sends the student's profile to the OpenAI Chat Completion API
     * and navigates to {@link CareerSuggestionsActivity} with the
     * parsed result.
     *
     * <h3>How it works</h3>
     * <ol>
     *   <li>{@link OpenAIRequest#buildCareerPrompt} constructs a system
     *       + user message pair with a strict JSON schema.</li>
     *   <li>The request is sent asynchronously via Retrofit's
     *       {@link Call#enqueue} (runs on a background thread).</li>
     *   <li>On success, the assistant's JSON content is parsed with
     *       Gson into a {@link OpenAIResponse.CareerListWrapper},
     *       converted to {@code ArrayList<Career>}, and passed to the
     *       next activity via Intent.</li>
     *   <li>On failure, a Toast displays the error.</li>
     * </ol>
     *
     * @param skills    Comma-separated skills string.
     * @param interests Comma-separated interests string.
     * @param gpa       Student GPA (0.0 – 4.0).
     * @param projects  Free-text description of academic projects.
     */
    private void callOpenAI(String skills, String interests,
                            double gpa, String projects) {

        // Disable the button to prevent duplicate requests
        btnGenerateSuggestions.setEnabled(false);
        btnGenerateSuggestions.setText(R.string.btn_generating);

        // 1. Build the request body
        OpenAIRequest request = OpenAIRequest.buildCareerPrompt(
                skills, interests, gpa, projects);

        // 2. Create the Retrofit service
        OpenAIService service = RetrofitClient.createService(OpenAIService.class);

        // 3. Enqueue the async call
        service.getChatCompletion(request).enqueue(new Callback<OpenAIResponse>() {

            @Override
            public void onResponse(@NonNull Call<OpenAIResponse> call,
                                   @NonNull Response<OpenAIResponse> response) {

                // Re-enable the button regardless of outcome
                btnGenerateSuggestions.setEnabled(true);
                btnGenerateSuggestions.setText(R.string.btn_generate_suggestions);

                if (response.isSuccessful() && response.body() != null) {

                    // Extract the JSON string from the assistant's message
                    String content = response.body().extractContent();

                    if (content != null) {
                        Log.d(TAG, "OpenAI raw content: " + content);

                        // Parse the JSON into Career objects
                        ArrayList<Career> careers = parseCareerJson(content);

                        if (careers != null && !careers.isEmpty()) {
                            // Navigate to CareerSuggestionsActivity with real data
                            Intent intent = new Intent(
                                    CareerProfileActivity.this,
                                    CareerSuggestionsActivity.class);
                            intent.putExtra("career_list", careers);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CareerProfileActivity.this,
                                    R.string.toast_openai_parse_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(CareerProfileActivity.this,
                                R.string.toast_openai_empty,
                                Toast.LENGTH_LONG).show();
                    }

                } else {
                    // HTTP error (4xx / 5xx)
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) { }

                    Log.e(TAG, "OpenAI error " + response.code() + ": " + errorBody);
                    Toast.makeText(CareerProfileActivity.this,
                            getString(R.string.toast_openai_http_error, response.code()),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenAIResponse> call,
                                  @NonNull Throwable t) {

                // Re-enable the button
                btnGenerateSuggestions.setEnabled(true);
                btnGenerateSuggestions.setText(R.string.btn_generate_suggestions);

                // Network error (timeout, no connection, DNS failure, etc.)
                Log.e(TAG, "OpenAI call failed", t);
                Toast.makeText(CareerProfileActivity.this,
                        getString(R.string.toast_openai_network_error, t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── JSON parsing helper ─────────────────────────────────────────

    /**
     * Parses the raw JSON string returned by the OpenAI model into
     * a list of {@link Career} objects.
     *
     * @param json The JSON string from the assistant (should match
     *             the schema defined in the system prompt).
     * @return An {@code ArrayList<Career>}, or {@code null} if parsing fails.
     */
    private ArrayList<Career> parseCareerJson(String json) {
        try {
            Gson gson = new Gson();
            OpenAIResponse.CareerListWrapper wrapper =
                    gson.fromJson(json, OpenAIResponse.CareerListWrapper.class);

            if (wrapper == null || wrapper.getCareers() == null) {
                return null;
            }

            // Convert each parsed entry into the app's Career model
            ArrayList<Career> careers = new ArrayList<>();
            for (OpenAIResponse.CareerListWrapper.CareerEntry entry : wrapper.getCareers()) {
                careers.add(new Career(
                        entry.name,
                        entry.matchScore,
                        entry.description,
                        new ArrayList<>(entry.skillGaps),
                        new ArrayList<>(entry.certifications)
                ));
            }
            return careers;

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse OpenAI JSON", e);
            return null;
        }
    }

    // ── Firestore Integration (placeholder) ─────────────────────────

    /**
     * Placeholder for saving the career profile to Cloud Firestore.
     * See previous implementation for full Javadoc and integration steps.
     */
    private void saveProfileToFirestore(String skills, String interests,
                                        double gpa, String projects) {
        // TODO: Implement Firestore document creation
    }
}
