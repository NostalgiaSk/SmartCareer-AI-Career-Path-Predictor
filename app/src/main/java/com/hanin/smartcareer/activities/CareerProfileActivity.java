package com.hanin.smartcareer.activities;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hanin.smartcareer.R;


public class CareerProfileActivity extends AppCompatActivity {

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

    private void attemptSubmitProfile() {

        // ── Read current input values (trim whitespace) ─────────────
        String skills    = etSkills.getText()    != null ? etSkills.getText().toString().trim()    : "";
        String interests = etInterests.getText() != null ? etInterests.getText().toString().trim() : "";
        String gpaStr    = etGpa.getText()       != null ? etGpa.getText().toString().trim()       : "";
        String projects  = etProjects.getText()  != null ? etProjects.getText().toString().trim()  : "";

        // ── Reset any previous error messages ───────────────────────
        tilSkills.setError(null);
        tilInterests.setError(null);
        tilGpa.setError(null);
        tilProjects.setError(null);

        boolean isValid = true;

        // ── 1. Skills validation ────────────────────────────────────
        if (skills.isEmpty()) {
            tilSkills.setError(getString(R.string.error_skills_empty));
            isValid = false;
        }

        // ── 2. Interests validation ─────────────────────────────────
        if (interests.isEmpty()) {
            tilInterests.setError(getString(R.string.error_interests_empty));
            isValid = false;
        }

        // ── 3. GPA validation ───────────────────────────────────────
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

        // ── 4. Projects validation ──────────────────────────────────
        if (projects.isEmpty()) {
            tilProjects.setError(getString(R.string.error_projects_empty));
            isValid = false;
        }

        // ── Proceed if all checks pass ──────────────────────────────
        if (isValid) {
            Toast.makeText(this, R.string.toast_profile_saved, Toast.LENGTH_SHORT).show();

            // Hand off to Firestore (placeholder)
            saveProfileToFirestore(skills, interests, gpa, projects);

            // Navigate to the AI career suggestions screen
            startActivity(new Intent(this, CareerSuggestionsActivity.class));
            finish();
        }
    }

    // ── Firestore Integration (placeholder) ─────────────────────────

    /**
     * Placeholder method for saving the career profile to Cloud Firestore.
     * <p>
     * To integrate Firestore:
     * <ol>
     *   <li>Add the Firebase Firestore SDK to your project
     *       ({@code implementation 'com.google.firebase:firebase-firestore'}).</li>
     *   <li>Get a {@code FirebaseFirestore} instance:
     *       {@code FirebaseFirestore db = FirebaseFirestore.getInstance();}.</li>
     *   <li>Retrieve the current user's UID from {@code FirebaseAuth}:
     *       {@code String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();}.</li>
     *   <li>Create a {@code Map<String, Object>} containing skills, interests,
     *       gpa, and projects.</li>
     *   <li>Write the document:
     *       {@code db.collection("career_profiles").document(uid).set(profileMap);}.</li>
     *   <li>Handle success → navigate to the career suggestions screen.</li>
     *   <li>Handle failure → display the error to the user.</li>
     * </ol>
     *
     * @param skills    Comma-separated skills string.
     * @param interests Comma-separated interests string.
     * @param gpa       The student's GPA (0.0 – 4.0).
     * @param projects  Free-text description of academic projects.
     */
    private void saveProfileToFirestore(String skills, String interests,
                                        double gpa, String projects) {
        // TODO: Implement Firestore document creation
        //
        // Example (uncomment when Firebase is configured):
        //
        // FirebaseFirestore db = FirebaseFirestore.getInstance();
        // String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //
        // Map<String, Object> profile = new HashMap<>();
        // profile.put("skills", skills);
        // profile.put("interests", interests);
        // profile.put("gpa", gpa);
        // profile.put("projects", projects);
        // profile.put("createdAt", FieldValue.serverTimestamp());
        //
        // db.collection("career_profiles")
        //   .document(uid)
        //   .set(profile)
        //   .addOnSuccessListener(aVoid -> {
        //       // Navigate to CareerSuggestionsActivity
        //       startActivity(new Intent(CareerProfileActivity.this,
        //                               CareerSuggestionsActivity.class));
        //       finish();
        //   })
        //   .addOnFailureListener(e -> {
        //       Toast.makeText(CareerProfileActivity.this,
        //               "Failed to save profile: " + e.getMessage(),
        //               Toast.LENGTH_LONG).show();
        //   });
    }
}
