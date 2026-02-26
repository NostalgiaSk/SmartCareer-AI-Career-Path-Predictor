package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.SavedCareersListAdapter;
import com.hanin.smartcareer.models.Career;

import java.util.ArrayList;
import java.util.List;

public class SavedCareersActivity extends AppCompatActivity {

    private static final String TAG = "SavedCareersActivity";


    private RecyclerView rvSavedCareers;
    private ProgressBar progressLoading;
    private LinearLayout emptyState;
    private ImageButton btnBack;

    private List<Career> savedCareers;
    private SavedCareersListAdapter adapter;

    // ── Firebase ─────────────────────────────────────────────────────

    private FirebaseFirestore db;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_careers);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        savedCareers = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupListeners();

        // Fetch data from Firestore
        fetchSavedCareers();
    }

 
    @Override
    protected void onResume() {
        super.onResume();
        // Re-fetch to catch any deletions made in CareerDetailActivity
        if (adapter != null) {
            fetchSavedCareers();
        }
    }


    private void initViews() {
        rvSavedCareers  = findViewById(R.id.rvSavedCareers);
        progressLoading = findViewById(R.id.progressLoading);
        emptyState      = findViewById(R.id.emptyState);
        btnBack         = findViewById(R.id.btnBack);
    }


    private void setupRecyclerView() {
        adapter = new SavedCareersListAdapter(this, savedCareers);
        rvSavedCareers.setLayoutManager(new LinearLayoutManager(this));
        rvSavedCareers.setAdapter(adapter);
    }


    private void setupListeners() {
        // Back button → finish activity
        btnBack.setOnClickListener(v -> finish());

        // Empty state CTA → navigate to CareerProfileActivity
        MaterialButton btnStartAnalysis = findViewById(R.id.btnStartAnalysis);
        btnStartAnalysis.setOnClickListener(v ->
                startActivity(new Intent(this, CareerProfileActivity.class)));
    }

    // ══════════════════════════════════════════════════════════════════
    // FIRESTORE DATA LOADING
    // ══════════════════════════════════════════════════════════════════

    private void fetchSavedCareers() {
        if (uid == null) {
            showEmptyState();
            return;
        }

        progressLoading.setVisibility(View.VISIBLE);
        rvSavedCareers.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        db.collection("users").document(uid)
                .collection("saved_careers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    savedCareers.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Map Firestore fields to Career model
                        Career career = new Career();
                        career.setSuggestionId(doc.getString("suggestionId"));
                        career.setCareerName(doc.getString("careerTitle"));
                        career.setDescription(doc.getString("description"));

                        Long score = doc.getLong("matchScore");
                        career.setMatchScore(score != null ? score.intValue() : 0);

                        savedCareers.add(career);
                    }

                    adapter.notifyDataSetChanged();
                    progressLoading.setVisibility(View.GONE);

                    if (savedCareers.isEmpty()) {
                        showEmptyState();
                    } else {
                        rvSavedCareers.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }

                    Log.d(TAG, "Loaded " + savedCareers.size() + " saved careers");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch saved careers", e);
                    progressLoading.setVisibility(View.GONE);
                    showEmptyState();
                    Toast.makeText(this,
                            "Could not load saved careers.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Empty state helper ──────────────────────────────────────────

    private void showEmptyState() {
        rvSavedCareers.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
}
