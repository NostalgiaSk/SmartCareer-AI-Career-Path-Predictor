package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.CertificationProgressAdapter;
import com.hanin.smartcareer.adapters.LatestCareerAdapter;
import com.hanin.smartcareer.adapters.SavedCareerAdapter;
import com.hanin.smartcareer.models.Career;
import com.hanin.smartcareer.models.CertificationProgress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main Home Dashboard — the first screen the user sees after login.
 *
 * Displays:
 * <ul>
 *   <li>Welcome greeting with user name</li>
 *   <li>Quick action cards (New Analysis, Saved, Certs, Progress)</li>
 *   <li>Latest AI-generated career suggestions (vertical RecyclerView)</li>
 *   <li>Saved careers (horizontal RecyclerView)</li>
 *   <li>Certifications in progress (vertical RecyclerView + ProgressBar)</li>
 * </ul>
 */
public class HomeActivity extends AppCompatActivity {

    // ── View references ─────────────────────────────────────────────

    private TextView tvWelcome;
    private ImageView ivProfile;
    private MaterialCardView cardNewAnalysis, cardSavedCareers,
            cardCertifications, cardProgress;
    private RecyclerView rvLatestCareers, rvSavedCareers, rvCertificationsProgress;

    // ── Data ─────────────────────────────────────────────────────────

    private List<Career> latestCareers;
    private List<Career> savedCareers;
    private List<CertificationProgress> certProgressList;

    // ── Lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupWelcome();
        setupQuickActions();
        loadDummyData();
        setupRecyclerViews();

        // Firebase placeholders — swap with live data when backend is ready
        fetchLatestCareers();
        fetchSavedCareers();
        fetchCertificationsProgress();
    }

    // ── View initialisation ──────────────────────────────────────────

    private void initViews() {
        tvWelcome     = findViewById(R.id.tvWelcome);
        ivProfile     = findViewById(R.id.ivProfile);

        cardNewAnalysis   = findViewById(R.id.cardNewAnalysis);
        cardSavedCareers  = findViewById(R.id.cardSavedCareers);
        cardCertifications = findViewById(R.id.cardCertifications);
        cardProgress      = findViewById(R.id.cardProgress);

        rvLatestCareers          = findViewById(R.id.rvLatestCareers);
        rvSavedCareers           = findViewById(R.id.rvSavedCareers);
        rvCertificationsProgress = findViewById(R.id.rvCertificationsProgress);
    }

    // ── Welcome greeting ─────────────────────────────────────────────

    private void setupWelcome() {
        // TODO: Replace with real user name from Firebase Auth
        // String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String userName = "Hanin";
        tvWelcome.setText(getString(R.string.home_welcome, userName));
    }

    // ── Quick action card clicks ─────────────────────────────────────

    private void setupQuickActions() {
        // Start New Analysis → navigate to CareerProfileActivity
        cardNewAnalysis.setOnClickListener(v ->
                startActivity(new Intent(this, CareerProfileActivity.class)));

        // View Saved Careers → placeholder
        cardSavedCareers.setOnClickListener(v ->
                Toast.makeText(this, "Saved Careers – coming soon!", Toast.LENGTH_SHORT).show());

        // My Certifications → placeholder
        cardCertifications.setOnClickListener(v ->
                Toast.makeText(this, "My Certifications – coming soon!", Toast.LENGTH_SHORT).show());

        // My Progress → placeholder
        cardProgress.setOnClickListener(v ->
                Toast.makeText(this, "My Progress – coming soon!", Toast.LENGTH_SHORT).show());
    }

    // ── RecyclerView setup ───────────────────────────────────────────

    private void setupRecyclerViews() {
        // Latest careers — vertical list
        rvLatestCareers.setLayoutManager(new LinearLayoutManager(this));
        rvLatestCareers.setAdapter(new LatestCareerAdapter(this, latestCareers));

        // Saved careers — horizontal list
        rvSavedCareers.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSavedCareers.setAdapter(new SavedCareerAdapter(this, savedCareers));

        // Certifications in progress — vertical list
        rvCertificationsProgress.setLayoutManager(new LinearLayoutManager(this));
        rvCertificationsProgress.setAdapter(
                new CertificationProgressAdapter(this, certProgressList));
    }

    // ── Dummy data (will be replaced by Firebase) ────────────────────

    private void loadDummyData() {
        // Latest career suggestions
        latestCareers = new ArrayList<>();
        latestCareers.add(new Career("AI Engineer", 92,
                "Design intelligent systems using machine learning and deep learning.",
                new ArrayList<>(Arrays.asList("TensorFlow", "PyTorch")),
                new ArrayList<>(Arrays.asList("Google ML Engineer", "AWS ML"))));
        latestCareers.add(new Career("Data Analyst", 87,
                "Transform raw data into actionable insights with statistical tools.",
                new ArrayList<>(Arrays.asList("SQL", "Power BI")),
                new ArrayList<>(Arrays.asList("Google Data Analytics", "IBM Analyst"))));

        // Saved careers
        savedCareers = new ArrayList<>();
        savedCareers.add(new Career("Product Manager", 85,
                "Lead product vision and roadmap execution.",
                new ArrayList<>(Arrays.asList("Agile", "UX")),
                new ArrayList<>(Arrays.asList("CSPO", "PMI-ACP"))));
        savedCareers.add(new Career("UX Designer", 80,
                "Create intuitive user experiences for digital products.",
                new ArrayList<>(Arrays.asList("Figma", "Research")),
                new ArrayList<>(Arrays.asList("Google UX", "Interaction Design"))));
        savedCareers.add(new Career("Cloud Architect", 78,
                "Design scalable cloud infrastructure solutions.",
                new ArrayList<>(Arrays.asList("AWS", "Kubernetes")),
                new ArrayList<>(Arrays.asList("AWS Solutions Architect", "GCP Pro"))));

        // Certifications in progress
        certProgressList = new ArrayList<>();
        certProgressList.add(new CertificationProgress(
                "Google ML Engineer", "Google", 65));
        certProgressList.add(new CertificationProgress(
                "AWS Solutions Architect", "Amazon", 40));
        certProgressList.add(new CertificationProgress(
                "Certified Scrum Product Owner", "Scrum Alliance", 20));
    }

    // ══════════════════════════════════════════════════════════════════
    // FIREBASE INTEGRATION PLACEHOLDERS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Placeholder — fetch the user's latest AI-generated career suggestions
     * from Firestore.
     *
     * Implementation steps:
     * 1. Get uid from FirebaseAuth.getInstance().getCurrentUser().getUid()
     * 2. Query: db.collection("career_suggestions")
     *           .whereEqualTo("userId", uid)
     *           .orderBy("createdAt", Query.Direction.DESCENDING)
     *           .limit(3)
     * 3. Parse documents into Career objects
     * 4. Update latestCareers list and call adapter.notifyDataSetChanged()
     */
    private void fetchLatestCareers() {
        // TODO: implement Firestore query
    }

    /**
     * Placeholder — fetch careers the user has bookmarked / saved.
     *
     * Implementation steps:
     * 1. Query: db.collection("saved_careers")
     *           .whereEqualTo("userId", uid)
     * 2. Parse into Career objects
     * 3. Update savedCareers list and notify adapter
     */
    private void fetchSavedCareers() {
        // TODO: implement Firestore query
    }

    /**
     * Placeholder — fetch the user's certification progress records.
     *
     * Implementation steps:
     * 1. Query: db.collection("certifications_progress")
     *           .whereEqualTo("userId", uid)
     * 2. Parse into CertificationProgress objects
     * 3. Update certProgressList and notify adapter
     */
    private void fetchCertificationsProgress() {
        // TODO: implement Firestore query
    }
}
