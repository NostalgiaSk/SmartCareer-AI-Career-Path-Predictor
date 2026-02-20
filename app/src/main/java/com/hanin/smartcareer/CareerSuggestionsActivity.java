package com.hanin.smartcareer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
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
        loadDummyCareers();
        setupRecyclerView();

        // Placeholder – swap with real Firestore call when backend is ready
        fetchCareerSuggestionsFromFirestore();
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

    // ── Dummy AI-generated career data ──────────────────────────────


    private void loadDummyCareers() {
        careerList = new ArrayList<>();

        careerList.add(new Career(
                "AI Engineer",
                92,
                "Design and build intelligent systems powered by machine learning and deep learning frameworks.",
                new ArrayList<>(Arrays.asList("TensorFlow", "PyTorch", "MLOps")),
                new ArrayList<>(Arrays.asList("Google ML Engineer", "AWS ML Specialty"))
        ));

        careerList.add(new Career(
                "Data Analyst",
                88,
                "Transform raw data into actionable insights using statistical analysis and visualization tools.",
                new ArrayList<>(Arrays.asList("SQL", "Power BI", "Pandas")),
                new ArrayList<>(Arrays.asList("Google Data Analytics", "IBM Data Analyst"))
        ));

        careerList.add(new Career(
                "Product Manager",
                85,
                "Lead cross-functional teams to define product vision, strategy, and roadmap execution.",
                new ArrayList<>(Arrays.asList("Agile / Scrum", "User Research", "A/B Testing")),
                new ArrayList<>(Arrays.asList("Certified Scrum Product Owner", "PMI-ACP"))
        ));
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
