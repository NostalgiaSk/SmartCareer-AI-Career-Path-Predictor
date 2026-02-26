package com.hanin.smartcareer.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.CareerAdapter;
import com.hanin.smartcareer.models.Career;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CareerSuggestionsActivity extends AppCompatActivity {

    private static final String TAG = "CareerSuggestionsAct";
    private RecyclerView rvCareers;
    private ProgressBar progressLoading;
    private LinearLayout emptyState;

    private CareerAdapter careerAdapter;
    private List<Career> careerList;
    private Set<String> savedIds;

    private FirebaseFirestore db;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career_suggestions);

        db = FirebaseFirestore.getInstance();
        careerList = new ArrayList<>();
        savedIds   = new HashSet<>();

        // Get the current user's UID for saved career checks
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initViews();

        // Get the profile ID from the Intent
        String profileId = getIntent().getStringExtra("profile_id");

        if (profileId != null && !profileId.isEmpty()) {
            // Preload saved IDs, then set up adapter and fetch suggestions
            preloadSavedIdsThenFetch(profileId);
        } else {
            // No profile ID — show empty state
            setupRecyclerView();
            showEmptyState();
            Toast.makeText(this, "No profile data available.", Toast.LENGTH_SHORT).show();
        }
    }


    private void initViews() {
        rvCareers       = findViewById(R.id.rvCareers);
        progressLoading = findViewById(R.id.progressLoading);
        emptyState      = findViewById(R.id.emptyState);
    }


    private void setupRecyclerView() {
        careerAdapter = new CareerAdapter(this, careerList, savedIds);
        rvCareers.setLayoutManager(new LinearLayoutManager(this));
        rvCareers.setAdapter(careerAdapter);
    }

    
    /**
     * Loads the user's saved career IDs first, then sets up the adapter
     * and fetches the AI suggestions. This ensures heart icons are
     * correct from the first render.
     */
    private void preloadSavedIdsThenFetch(String profileId) {
        progressLoading.setVisibility(View.VISIBLE);

        if (uid == null) {
            // No authenticated user — just set up and fetch
            setupRecyclerView();
            fetchSuggestionsFromFirestore(profileId);
            return;
        }

        db.collection("users").document(uid)
                .collection("saved_careers")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        savedIds.add(doc.getId());
                    }
                    setupRecyclerView();
                    fetchSuggestionsFromFirestore(profileId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to preload saved IDs", e);
                    setupRecyclerView();
                    fetchSuggestionsFromFirestore(profileId);
                });
    }

    /**
     * Loads AI suggestions from the Firestore subcollection:
     * career_profiles/{profileId}/ai_suggestions
     *
     * Ordered by matchScore descending so the best match appears first.
     */
    private void fetchSuggestionsFromFirestore(String profileId) {
        // Show loading, hide everything else
        progressLoading.setVisibility(View.VISIBLE);
        rvCareers.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        db.collection("career_profiles")
                .document(profileId)
                .collection("ai_suggestions")
                .orderBy("matchScore", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    careerList.clear();

                    // Parse each Firestore document into a Career object
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Career career = doc.toObject(Career.class);
                        if (career != null) {
                            // Set the Firestore document ID as suggestionId
                            career.setSuggestionId(doc.getId());
                            careerList.add(career);
                        }
                    }

                    careerAdapter.notifyDataSetChanged();
                    progressLoading.setVisibility(View.GONE);

                    if (careerList.isEmpty()) {
                        showEmptyState();
                    } else {
                        rvCareers.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch suggestions", e);
                    progressLoading.setVisibility(View.GONE);
                    showEmptyState();
                    Toast.makeText(this,
                            "Could not load career suggestions.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Empty state helper ──────────────────────────────────────────

    private void showEmptyState() {
        rvCareers.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
}
