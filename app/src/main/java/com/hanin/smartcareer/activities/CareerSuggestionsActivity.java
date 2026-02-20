package com.hanin.smartcareer.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.CareerAdapter;
import com.hanin.smartcareer.models.Career;

import java.util.ArrayList;
import java.util.List;


public class CareerSuggestionsActivity extends AppCompatActivity {

    // ── View references ─────────────────────────────────────────────

    private RecyclerView rvCareers;
    private CareerAdapter careerAdapter;
    private List<Career> careerList;

    // ── Lifecycle ───────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career_suggestions);

        initViews();

        // Get AI-generated career data from the Intent
        ArrayList<Career> intentCareers =
                (ArrayList<Career>) getIntent().getSerializableExtra("career_list");

        if (intentCareers != null && !intentCareers.isEmpty()) {
            careerList = intentCareers;
        } else {
            // No data received — show empty state
            careerList = new ArrayList<>();
            Toast.makeText(this, "No career suggestions available.", Toast.LENGTH_SHORT).show();
        }

        setupRecyclerView();
    }

    // ── View initialisation ─────────────────────────────────────────

    private void initViews() {
        rvCareers = findViewById(R.id.rvCareers);
    }

    // ── RecyclerView setup ──────────────────────────────────────────

    private void setupRecyclerView() {
        careerAdapter = new CareerAdapter(this, careerList);
        rvCareers.setLayoutManager(new LinearLayoutManager(this));
        rvCareers.setAdapter(careerAdapter);
    }

    // ── Firestore Integration (placeholder) ─────────────────────────

    /**
     * Placeholder for loading career suggestions from Cloud Firestore.
     * <p>
     * Integration steps:
     * <ol>
     *   <li>Add the Firebase Firestore SDK to {@code build.gradle}:
     *       {@code implementation 'com.google.firebase:firebase-firestore'}.</li>
     *
     *   <li>Obtain the current user's UID from Firebase Auth:
     *       {@code String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();}.</li>
     *
     *   <li>Query the user's personalised career matches:
     *       <pre>
     *       FirebaseFirestore db = FirebaseFirestore.getInstance();
     *       db.collection("career_suggestions")
     *         .whereEqualTo("userId", uid)
     *         .orderBy("matchScore", Query.Direction.DESCENDING)
     *         .get()
     *         .addOnSuccessListener(querySnapshot -> {
     *             careerList.clear();
     *             for (DocumentSnapshot doc : querySnapshot) {
     *                 Career c = doc.toObject(Career.class);
     *                 careerList.add(c);
     *             }
     *             careerAdapter.notifyDataSetChanged();
     *         })
     *         .addOnFailureListener(e -> {
     *             // Show error state or retry prompt
     *         });
     *       </pre></li>
     *
     *   <li>Optionally show a loading spinner while the query is in flight.</li>
     * </ol>
     */
    private void fetchCareerSuggestionsFromFirestore() {
        // TODO: Replace loadDummyCareers() call with the Firestore query above.
    }
}
