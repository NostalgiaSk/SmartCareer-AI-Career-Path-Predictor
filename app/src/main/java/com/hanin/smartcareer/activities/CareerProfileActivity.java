package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Career;
import com.hanin.smartcareer.models.OpenAIRequest;
import com.hanin.smartcareer.models.OpenAIResponse;
import com.hanin.smartcareer.network.OpenAIService;
import com.hanin.smartcareer.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Collects the student's career profile (skills, interests, GPA,
 * projects), sends it to the OpenAI API, saves the profile + AI
 * suggestions to Firestore, and navigates to CareerSuggestionsActivity.
 *
 * Flow:
 * 1. User fills in the form and taps "Generate Career Suggestions".
 * 2. OpenAI returns 5 career suggestions as JSON.
 * 3. Profile + all suggestions are saved to Firestore via batch write.
 * 4. On success → navigate to CareerSuggestionsActivity with profileId.
 */
public class CareerProfileActivity extends AppCompatActivity {

    private static final String TAG = "CareerProfileActivity";

    // ── View references ─────────────────────────────────────────────

    private TextInputLayout tilSkills, tilInterests, tilGpa, tilProjects;
    private TextInputEditText etSkills, etInterests, etGpa, etProjects;
    private Button btnGenerateSuggestions;
    private ProgressBar progressSaving;

    // ── Firebase ─────────────────────────────────────────────────────

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // ── Lifecycle ───────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career_profile);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

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
        progressSaving         = findViewById(R.id.progressSaving);
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
            // Call the OpenAI API to generate career suggestions
            callOpenAI(skills, interests, gpa, projects);
        }
    }

    // ── Loading state helper ────────────────────────────────────────

    private void setLoading(boolean loading) {
        btnGenerateSuggestions.setEnabled(!loading);
        btnGenerateSuggestions.setText(loading ? R.string.btn_generating : R.string.btn_generate_suggestions);
        progressSaving.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    // ══════════════════════════════════════════════════════════════════
    // OPENAI API CALL
    // ══════════════════════════════════════════════════════════════════

    private void callOpenAI(String skills, String interests,
                            double gpa, String projects) {

        // Disable the button and show spinner
        setLoading(true);

        // Build the request body
        OpenAIRequest request = OpenAIRequest.buildCareerPrompt(
                skills, interests, gpa, projects);

        // Create the Retrofit service and enqueue the async call
        OpenAIService service = RetrofitClient.createService(OpenAIService.class);

        service.getChatCompletion(request).enqueue(new Callback<OpenAIResponse>() {

            @Override
            public void onResponse(@NonNull Call<OpenAIResponse> call,
                                   @NonNull Response<OpenAIResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    // Extract the JSON string from the assistant's message
                    String content = response.body().extractContent();

                    if (content != null) {
                        Log.d(TAG, "OpenAI raw content: " + content);

                        // Parse the JSON into Career objects
                        ArrayList<Career> careers = parseCareerJson(content);

                        if (careers != null && !careers.isEmpty()) {
                            // Save profile + suggestions to Firestore
                            saveToFirestore(skills, interests, gpa, projects, careers);
                        } else {
                            setLoading(false);
                            Toast.makeText(CareerProfileActivity.this,
                                    R.string.toast_openai_parse_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        setLoading(false);
                        Toast.makeText(CareerProfileActivity.this,
                                R.string.toast_openai_empty,
                                Toast.LENGTH_LONG).show();
                    }

                } else {
                    setLoading(false);
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
                setLoading(false);

                // Network error (timeout, no connection, DNS failure, etc.)
                Log.e(TAG, "OpenAI call failed", t);
                Toast.makeText(CareerProfileActivity.this,
                        getString(R.string.toast_openai_network_error, t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── JSON parsing helper ─────────────────────────────────────────

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

    // ══════════════════════════════════════════════════════════════════
    // FIRESTORE BATCH WRITE
    // ══════════════════════════════════════════════════════════════════

    /**
     * Saves the career profile document and ALL AI suggestions as a
     * subcollection using a Firestore batch write (atomic operation).
     *
     * Structure:
     *   career_profiles/{profileId}
     *     └── ai_suggestions/{suggestionId}
     */
    private void saveToFirestore(String skills, String interests,
                                 double gpa, String projects,
                                 ArrayList<Career> careers) {

        // Get current user's UID
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            setLoading(false);
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();

        // Generate a new profile document reference
        DocumentReference profileRef = db.collection("career_profiles").document();
        String profileId = profileRef.getId();

        // Build the profile document data
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("profileId", profileId);
        profileData.put("userId", uid);
        profileData.put("skills", Arrays.asList(skills.split("\\s*,\\s*")));      // "Java, Python" → ["Java","Python"]
        profileData.put("interests", Arrays.asList(interests.split("\\s*,\\s*")));
        profileData.put("gpa", gpa);
        profileData.put("projects", projects);
        profileData.put("createdAt", FieldValue.serverTimestamp());

        // Create a batch write — all documents succeed or all fail together
        WriteBatch batch = db.batch();

        // 1. Set the profile document
        batch.set(profileRef, profileData);

        // 2. Add each AI suggestion to the "ai_suggestions" subcollection
        for (int i = 0; i < careers.size(); i++) {
            Career career = careers.get(i);

            // Generate a new suggestion document reference
            DocumentReference suggestionRef = profileRef
                    .collection("ai_suggestions")
                    .document();

            Map<String, Object> suggestionData = new HashMap<>();
            suggestionData.put("suggestionId", suggestionRef.getId());
            suggestionData.put("careerName", career.getCareerName());
            suggestionData.put("description", career.getDescription());
            suggestionData.put("matchScore", career.getMatchScore());
            suggestionData.put("skillGaps", career.getSkillGaps());
            suggestionData.put("certifications", career.getCertifications());
            suggestionData.put("aiGenerated", true);
            suggestionData.put("createdAt", FieldValue.serverTimestamp());

            batch.set(suggestionRef, suggestionData);
        }

        // 3. Commit the batch — atomic write
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Log.d(TAG, "Firestore batch write successful, profileId: " + profileId);

                    // Navigate to CareerSuggestionsActivity with the profileId
                    Intent intent = new Intent(
                            CareerProfileActivity.this,
                            CareerSuggestionsActivity.class);
                    intent.putExtra("profile_id", profileId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Log.e(TAG, "Firestore batch write failed", e);
                    Toast.makeText(CareerProfileActivity.this,
                            "Failed to save career data. Please try again.",
                            Toast.LENGTH_LONG).show();
                });
    }
}
