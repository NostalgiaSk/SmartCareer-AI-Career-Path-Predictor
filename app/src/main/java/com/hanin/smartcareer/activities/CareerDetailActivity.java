package com.hanin.smartcareer.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;

import java.util.List;

public class CareerDetailActivity extends AppCompatActivity {

    private static final String TAG = "CareerDetailActivity";

    // ── View references ─────────────────────────────────────────────

    private ImageButton btnBack;
    private TextView tvCareerTitle, tvMatchScore, tvDescription;
    private TextView tvRequiredSkills, tvMissingSkills, tvCertifications;
    private MaterialButton btnDelete;
    private ProgressBar progressLoading;

    // ── Firebase ─────────────────────────────────────────────────────

    private FirebaseFirestore db;
    private String uid;
    private String suggestionId;

    // ── Lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Get the suggestion ID from the Intent
        suggestionId = getIntent().getStringExtra("suggestion_id");

        initViews();
        setupListeners();

        if (suggestionId != null && uid != null) {
            fetchCareerDetail();
        } else {
            Toast.makeText(this, "Career data not available.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ── View initialisation ─────────────────────────────────────────

    private void initViews() {
        btnBack          = findViewById(R.id.btnBack);
        tvCareerTitle    = findViewById(R.id.tvCareerTitle);
        tvMatchScore     = findViewById(R.id.tvMatchScore);
        tvDescription    = findViewById(R.id.tvDescription);
        tvRequiredSkills = findViewById(R.id.tvRequiredSkills);
        tvMissingSkills  = findViewById(R.id.tvMissingSkills);
        tvCertifications = findViewById(R.id.tvCertifications);
        btnDelete        = findViewById(R.id.btnDelete);
        progressLoading  = findViewById(R.id.progressLoading);
    }

    // ── Click listeners ─────────────────────────────────────────────

    private void setupListeners() {
        // Back button → close this screen
        btnBack.setOnClickListener(v -> finish());

        // Delete button → remove from Firestore, then close
        btnDelete.setOnClickListener(v -> deleteCareer());
    }

    // ══════════════════════════════════════════════════════════════════
    // FIRESTORE DATA LOADING
    // ══════════════════════════════════════════════════════════════════

    /**
     * Fetches full career details from:
     *   users/{uid}/saved_careers/{suggestionId}
     *
     * Populates all TextViews with the Firestore document data.
     */
    private void fetchCareerDetail() {
        progressLoading.setVisibility(View.VISIBLE);
        btnDelete.setEnabled(false);

        db.collection("users").document(uid)
                .collection("saved_careers").document(suggestionId)
                .get()
                .addOnSuccessListener(doc -> {
                    progressLoading.setVisibility(View.GONE);

                    if (!doc.exists()) {
                        Toast.makeText(this, "Career not found.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // ── Populate the UI ──────────────────────────────────
                    tvCareerTitle.setText(
                            doc.getString("careerTitle") != null
                                    ? doc.getString("careerTitle") : "Unknown Career");

                    Long score = doc.getLong("matchScore");
                    tvMatchScore.setText(
                            (score != null ? score : 0) + "% Match");

                    tvDescription.setText(
                            doc.getString("description") != null
                                    ? doc.getString("description") : "No description available.");

                    // Format skills lists as bullet points
                    tvRequiredSkills.setText(
                            formatList(doc.get("requiredSkills")));

                    tvMissingSkills.setText(
                            formatList(doc.get("missingSkills")));

                    tvCertifications.setText(
                            formatList(doc.get("recommendedCertifications")));

                    btnDelete.setEnabled(true);

                    Log.d(TAG, "Loaded career detail: " + doc.getString("careerTitle"));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch career detail", e);
                    progressLoading.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Could not load career details.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ══════════════════════════════════════════════════════════════════
    // DELETE CAREER
    // ══════════════════════════════════════════════════════════════════

    /**
     * Deletes this career from users/{uid}/saved_careers/{suggestionId}.
     * Shows a toast and finishes the activity on success.
     */
    private void deleteCareer() {
        if (uid == null || suggestionId == null) return;

        btnDelete.setEnabled(false);
        btnDelete.setText("Removing...");

        db.collection("users").document(uid)
                .collection("saved_careers").document(suggestionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Career removed from saved.",
                            Toast.LENGTH_SHORT).show();
                    finish(); // Go back to SavedCareersActivity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete career", e);
                    btnDelete.setEnabled(true);
                    btnDelete.setText("Remove from Saved");
                    Toast.makeText(this, "Error removing career.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPER: FORMAT LIST AS BULLET POINTS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Converts a Firestore field (expected to be a List<String>) into
     * a bullet-pointed string for display.
     *
     * @param field The raw Firestore field value.
     * @return Formatted string like "• Item 1\n• Item 2", or a placeholder.
     */
    @SuppressWarnings("unchecked")
    private String formatList(Object field) {
        if (field == null) {
            return "Not available";
        }

        if (field instanceof List) {
            List<String> items = (List<String>) field;
            if (items.isEmpty()) return "Not available";

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                sb.append("• ").append(items.get(i));
                if (i < items.size() - 1) sb.append("\n");
            }
            return sb.toString();
        }

        return field.toString();
    }
}
