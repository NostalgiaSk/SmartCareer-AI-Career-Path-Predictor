package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.CertificationProgressAdapter;
import com.hanin.smartcareer.adapters.LatestCareerAdapter;
import com.hanin.smartcareer.adapters.SavedCareerAdapter;
import com.hanin.smartcareer.models.Career;
import com.hanin.smartcareer.models.CertificationProgress;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // ── View references ─────────────────────────────────────────────

    private TextView tvWelcome;
    private ImageView ivProfile;
    private MaterialCardView cardNewAnalysis, cardSavedCareers,
            cardCertifications, cardProgress;
    private RecyclerView rvLatestCareers, rvSavedCareers, rvCertificationsProgress;

    // Top Career Match card views
    private MaterialCardView cardTopMatch;
    private TextView tvTopMatchName, tvTopMatchScore, tvTopMatchDesc;

    // Loading spinners for each section
    private ProgressBar progressLatest, progressSaved, progressCerts;

    // Empty state containers for each section
    private LinearLayout emptyLatest, emptySaved, emptyCerts;

    // ── Data ─────────────────────────────────────────────────────────

    private List<Career> latestCareers;
    private List<Career> savedCareers;
    private List<CertificationProgress> certProgressList;

    // ── Adapters ─────────────────────────────────────────────────────

    private LatestCareerAdapter latestAdapter;
    private SavedCareerAdapter savedAdapter;
    private CertificationProgressAdapter certAdapter;

    // ── Firebase ─────────────────────────────────────────────────────

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;

    // ── Lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in — redirect to Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        uid = currentUser.getUid();

        // Initialize empty data lists
        latestCareers   = new ArrayList<>();
        savedCareers    = new ArrayList<>();
        certProgressList = new ArrayList<>();

        initViews();
        setupWelcome();
        setupQuickActions();
        setupEmptyStateButtons();
        setupRecyclerViews();

        // Fetch real data from Firestore
        fetchLatestCareers();
        fetchSavedCareers();
        fetchCertificationsProgress();
    }

    // ── View initialisation ──────────────────────────────────────────

    private void initViews() {
        tvWelcome     = findViewById(R.id.tvWelcome);
        ivProfile     = findViewById(R.id.ivProfile);

        // Top Career Match card
        cardTopMatch    = findViewById(R.id.cardTopMatch);
        tvTopMatchName  = findViewById(R.id.tvTopMatchName);
        tvTopMatchScore = findViewById(R.id.tvTopMatchScore);
        tvTopMatchDesc  = findViewById(R.id.tvTopMatchDesc);

        // Quick action cards
        cardNewAnalysis    = findViewById(R.id.cardNewAnalysis);
        cardSavedCareers   = findViewById(R.id.cardSavedCareers);
        cardCertifications = findViewById(R.id.cardCertifications);
        cardProgress       = findViewById(R.id.cardProgress);

        // RecyclerViews
        rvLatestCareers          = findViewById(R.id.rvLatestCareers);
        rvSavedCareers           = findViewById(R.id.rvSavedCareers);
        rvCertificationsProgress = findViewById(R.id.rvCertificationsProgress);

        // Loading spinners
        progressLatest = findViewById(R.id.progressLatest);
        progressSaved  = findViewById(R.id.progressSaved);
        progressCerts  = findViewById(R.id.progressCerts);

        // Empty state containers
        emptyLatest = findViewById(R.id.emptyLatest);
        emptySaved  = findViewById(R.id.emptySaved);
        emptyCerts  = findViewById(R.id.emptyCerts);

        // Hide the top match card until data loads
        cardTopMatch.setVisibility(View.GONE);
    }

    // ── Welcome greeting ─────────────────────────────────────────────

    private void setupWelcome() {
        // Use display name from Firebase Auth, with a fallback
        FirebaseUser user = auth.getCurrentUser();
        String userName = "Student";
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            userName = user.getDisplayName();
        }
        tvWelcome.setText(getString(R.string.home_welcome, userName));
    }

    // ── Quick action card clicks ─────────────────────────────────────

    private void setupQuickActions() {
        cardNewAnalysis.setOnClickListener(v ->
                startActivity(new Intent(this, CareerProfileActivity.class)));

        cardSavedCareers.setOnClickListener(v ->
                Toast.makeText(this, "Saved Careers – coming soon!", Toast.LENGTH_SHORT).show());

        cardCertifications.setOnClickListener(v ->
                Toast.makeText(this, "My Certifications – coming soon!", Toast.LENGTH_SHORT).show());

        cardProgress.setOnClickListener(v ->
                Toast.makeText(this, "My Progress – coming soon!", Toast.LENGTH_SHORT).show());
    }

    // ── Empty state button clicks ────────────────────────────────────

    private void setupEmptyStateButtons() {
        // All empty state CTA buttons navigate to CareerProfileActivity
        MaterialButton btnEmptyLatest = findViewById(R.id.btnEmptyLatest);
        MaterialButton btnEmptySaved  = findViewById(R.id.btnEmptySaved);
        MaterialButton btnEmptyCerts  = findViewById(R.id.btnEmptyCerts);

        View.OnClickListener goToProfile = v ->
                startActivity(new Intent(this, CareerProfileActivity.class));

        btnEmptyLatest.setOnClickListener(goToProfile);
        btnEmptySaved.setOnClickListener(goToProfile);
        btnEmptyCerts.setOnClickListener(goToProfile);
    }

    // ── RecyclerView setup ───────────────────────────────────────────

    private void setupRecyclerViews() {
        // Latest careers — vertical list
        rvLatestCareers.setLayoutManager(new LinearLayoutManager(this));
        latestAdapter = new LatestCareerAdapter(this, latestCareers);
        rvLatestCareers.setAdapter(latestAdapter);

        // Saved careers — horizontal list
        rvSavedCareers.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        savedAdapter = new SavedCareerAdapter(this, savedCareers);
        rvSavedCareers.setAdapter(savedAdapter);

        // Certifications in progress — vertical list
        rvCertificationsProgress.setLayoutManager(new LinearLayoutManager(this));
        certAdapter = new CertificationProgressAdapter(this, certProgressList);
        rvCertificationsProgress.setAdapter(certAdapter);
    }

    // ── Top Career Match card ────────────────────────────────────────

    private void populateTopMatchCard() {
        if (latestCareers == null || latestCareers.isEmpty()) {
            cardTopMatch.setVisibility(View.GONE);
            return;
        }

        // Find the career with the highest match score
        Career topMatch = latestCareers.get(0);
        for (Career c : latestCareers) {
            if (c.getMatchScore() > topMatch.getMatchScore()) {
                topMatch = c;
            }
        }

        tvTopMatchName.setText(topMatch.getCareerName());
        tvTopMatchScore.setText(topMatch.getMatchScore() + "%");
        tvTopMatchDesc.setText(topMatch.getDescription());

        // Tap the card → navigate to SkillGapActivity with career data
        Career finalTopMatch = topMatch;
        cardTopMatch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SkillGapActivity.class);
            intent.putExtra("career_data", finalTopMatch);
            startActivity(intent);
        });

        cardTopMatch.setVisibility(View.VISIBLE);
    }

    // ── Entry animations ─────────────────────────────────────────────

    private void applyEntryAnimations() {
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(
                this, R.anim.layout_animation_fall_down);

        rvLatestCareers.setLayoutAnimation(animation);
        rvCertificationsProgress.setLayoutAnimation(animation);

        rvLatestCareers.scheduleLayoutAnimation();
        rvCertificationsProgress.scheduleLayoutAnimation();
    }

    // ══════════════════════════════════════════════════════════════════
    // FIRESTORE DATA LOADING
    // ══════════════════════════════════════════════════════════════════

    // Fetch latest career profiles from "career_profiles" collection
    private void fetchLatestCareers() {
        progressLatest.setVisibility(View.VISIBLE);
        emptyLatest.setVisibility(View.GONE);

        db.collection("career_profiles")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    latestCareers.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Career career = doc.toObject(Career.class);
                        if (career != null) {
                            latestCareers.add(career);
                        }
                    }

                    latestAdapter.notifyDataSetChanged();
                    progressLatest.setVisibility(View.GONE);

                    if (latestCareers.isEmpty()) {
                        emptyLatest.setVisibility(View.VISIBLE);
                        rvLatestCareers.setVisibility(View.GONE);
                    } else {
                        emptyLatest.setVisibility(View.GONE);
                        rvLatestCareers.setVisibility(View.VISIBLE);
                        populateTopMatchCard();
                        applyEntryAnimations();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch latest careers", e);
                    progressLatest.setVisibility(View.GONE);
                    emptyLatest.setVisibility(View.VISIBLE);
                    rvLatestCareers.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not load career profiles.", Toast.LENGTH_SHORT).show();
                });
    }

    // Fetch saved careers from "users/{uid}/saved_careers" subcollection
    private void fetchSavedCareers() {
        progressSaved.setVisibility(View.VISIBLE);
        emptySaved.setVisibility(View.GONE);

        db.collection("users").document(uid)
                .collection("saved_careers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    savedCareers.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Career career = doc.toObject(Career.class);
                        if (career != null) {
                            savedCareers.add(career);
                        }
                    }

                    savedAdapter.notifyDataSetChanged();
                    progressSaved.setVisibility(View.GONE);

                    if (savedCareers.isEmpty()) {
                        emptySaved.setVisibility(View.VISIBLE);
                        rvSavedCareers.setVisibility(View.GONE);
                    } else {
                        emptySaved.setVisibility(View.GONE);
                        rvSavedCareers.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch saved careers", e);
                    progressSaved.setVisibility(View.GONE);
                    emptySaved.setVisibility(View.VISIBLE);
                    rvSavedCareers.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not load saved careers.", Toast.LENGTH_SHORT).show();
                });
    }

    // Fetch certifications from "users/{uid}/certifications" subcollection
    private void fetchCertificationsProgress() {
        progressCerts.setVisibility(View.VISIBLE);
        emptyCerts.setVisibility(View.GONE);

        db.collection("users").document(uid)
                .collection("certifications")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    certProgressList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        CertificationProgress cert = doc.toObject(CertificationProgress.class);
                        if (cert != null) {
                            certProgressList.add(cert);
                        }
                    }

                    certAdapter.notifyDataSetChanged();
                    progressCerts.setVisibility(View.GONE);

                    if (certProgressList.isEmpty()) {
                        emptyCerts.setVisibility(View.VISIBLE);
                        rvCertificationsProgress.setVisibility(View.GONE);
                    } else {
                        emptyCerts.setVisibility(View.GONE);
                        rvCertificationsProgress.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch certifications", e);
                    progressCerts.setVisibility(View.GONE);
                    emptyCerts.setVisibility(View.VISIBLE);
                    rvCertificationsProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not load certifications.", Toast.LENGTH_SHORT).show();
                });
    }
}
