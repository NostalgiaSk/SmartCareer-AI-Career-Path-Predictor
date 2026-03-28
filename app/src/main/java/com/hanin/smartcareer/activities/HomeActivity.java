package com.hanin.smartcareer.activities;

import android.app.AlertDialog;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";


    private TextView tvWelcome;
    private ImageView ivProfile;
    private MaterialCardView cardNewAnalysis, cardSavedCareers,
            cardCertifications, cardProgress;
    private RecyclerView rvLatestCareers, rvSavedCareers, rvCertificationsProgress;

    private MaterialCardView cardTopMatch;
    private TextView tvTopMatchName, tvTopMatchScore, tvTopMatchDesc;

    private ProgressBar progressLatest, progressSaved, progressCerts;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Empty state containers for each section
    private LinearLayout emptyLatest, emptySaved, emptyCerts;


    private List<Career> latestCareers;
    private List<Career> savedCareers;
    private List<CertificationProgress> certProgressList;
    private Set<String> savedCareerIds;   // Pre-loaded set of saved career IDs
    private String latestProfileId;       // Firestore ID of the latest career profile

    private LatestCareerAdapter latestAdapter;
    private SavedCareerAdapter savedAdapter;
    private CertificationProgressAdapter certAdapter;


    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;
    private boolean isFirstLaunch = true;


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

       
        latestCareers    = new ArrayList<>();
        savedCareers     = new ArrayList<>();
        certProgressList = new ArrayList<>();
        savedCareerIds   = new HashSet<>();

        initViews();
        setupWelcome();
        setupQuickActions();
        setupEmptyStateButtons();

        
        preloadSavedIdsAndFetch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Skip refresh on first launch (onCreate already fetches data)
        if (isFirstLaunch) {
            isFirstLaunch = false;
            return;
        }
        // Auto-refresh when returning from CareerProfileActivity or any other screen
        refreshAllData();
    }

   

    private void initViews() {
        tvWelcome     = findViewById(R.id.tvWelcome);
        ivProfile     = findViewById(R.id.ivProfile);

        // Logout button in hero header
        ImageView ivLogout = findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Pull-to-refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.sc_primary, R.color.sc_primary_dark, R.color.sc_secondary);
        swipeRefreshLayout.setOnRefreshListener(this::refreshAllData);

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

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.home_logout))
                .setMessage(getString(R.string.home_logout_confirm))
                .setPositiveButton(getString(R.string.home_logout), (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.home_logout_cancel), null)
                .show();
    }

    private void setupWelcome() {
        FirebaseUser user = auth.getCurrentUser();
        String userName = "Student";
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            userName = user.getDisplayName();
        }
        tvWelcome.setText(getString(R.string.home_welcome, userName));
    }


    private void setupQuickActions() {
        cardNewAnalysis.setOnClickListener(v ->
                startActivity(new Intent(this, CareerProfileActivity.class)));

        cardSavedCareers.setOnClickListener(v ->
                startActivity(new Intent(this, SavedCareersActivity.class)));

        cardCertifications.setOnClickListener(v ->
                startActivity(new Intent(this, ObtainedCertificationsActivity.class)));

        cardProgress.setOnClickListener(v ->
                Toast.makeText(this, "My Progress – coming soon!", Toast.LENGTH_SHORT).show());
    }

    // ── Empty state button clicks ────────────────────────────────────

    private void setupEmptyStateButtons() {
        MaterialButton btnEmptyLatest = findViewById(R.id.btnEmptyLatest);
        MaterialButton btnEmptySaved  = findViewById(R.id.btnEmptySaved);
        MaterialButton btnEmptyCerts  = findViewById(R.id.btnEmptyCerts);

        View.OnClickListener goToProfile = v ->
                startActivity(new Intent(this, CareerProfileActivity.class));

        btnEmptyLatest.setOnClickListener(goToProfile);
        btnEmptySaved.setOnClickListener(goToProfile);
        btnEmptyCerts.setOnClickListener(goToProfile);
    }

    // ── Refresh all data from Firebase ────────────────────────────────

    /**
     * Clears existing data and re-fetches everything from Firebase.
     * Called by SwipeRefreshLayout on manual pull and by onResume() for auto-refresh.
     */
    private void refreshAllData() {
        // Clear existing data
        latestCareers.clear();
        savedCareers.clear();
        certProgressList.clear();
        savedCareerIds.clear();

        // Notify adapters of clear (if they exist)
        if (latestAdapter != null) latestAdapter.notifyDataSetChanged();
        if (savedAdapter != null) savedAdapter.notifyDataSetChanged();
        if (certAdapter != null) certAdapter.notifyDataSetChanged();

        // Reset top match card
        cardTopMatch.setVisibility(View.GONE);

        // Re-fetch everything
        preloadSavedIdsAndFetch();
    }

    // ── Preload saved IDs, then set up adapters ──────────────────────


    private void preloadSavedIdsAndFetch() {
        db.collection("users").document(uid)
                .collection("saved_careers")
                .get()
                .addOnSuccessListener(snapshot -> {
                    // Populate the saved IDs set
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        savedCareerIds.add(doc.getId());
                    }
                    Log.d(TAG, "Preloaded " + savedCareerIds.size() + " saved career IDs");

                    // Now set up adapters and fetch data
                    setupRecyclerViews();
                    fetchLatestCareers();
                    fetchSavedCareers();
                    fetchCertificationsProgress();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to preload saved IDs", e);
                    // Still set up adapters with empty set
                    setupRecyclerViews();
                    fetchLatestCareers();
                    fetchSavedCareers();
                    fetchCertificationsProgress();
                });
    }

    // ── RecyclerView setup ───────────────────────────────────────────

    private void setupRecyclerViews() {
        // Latest careers — vertical list (savedIds + profileId set later)
        rvLatestCareers.setLayoutManager(new LinearLayoutManager(this));
        latestAdapter = new LatestCareerAdapter(this, latestCareers, savedCareerIds, null);
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

    // Fetch latest career suggestions from the most recent profile's "ai_suggestions" subcollection
    private void fetchLatestCareers() {
        progressLatest.setVisibility(View.VISIBLE);
        emptyLatest.setVisibility(View.GONE);

        // Step 1: Get all career profiles for this user
        db.collection("career_profiles")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(profileSnapshot -> {

                    if (profileSnapshot.isEmpty()) {
                        progressLatest.setVisibility(View.GONE);
                        emptyLatest.setVisibility(View.VISIBLE);
                        rvLatestCareers.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }

                    // Find the profile with the latest createdAt timestamp
                    DocumentSnapshot latestProfile = profileSnapshot.getDocuments().get(0);
                    for (DocumentSnapshot doc : profileSnapshot.getDocuments()) {
                        com.google.firebase.Timestamp ts = doc.getTimestamp("createdAt");
                        com.google.firebase.Timestamp latestTs = latestProfile.getTimestamp("createdAt");
                        if (ts != null && (latestTs == null || ts.compareTo(latestTs) > 0)) {
                            latestProfile = doc;
                        }
                    }

                    // Step 2: Store the profile ID and recreate the adapter with it
                    latestProfileId = latestProfile.getId();
                    latestAdapter = new LatestCareerAdapter(
                            this, latestCareers, savedCareerIds, latestProfileId);
                    rvLatestCareers.setAdapter(latestAdapter);

                    Log.d(TAG, "Loading ai_suggestions from profile: " + latestProfileId);

                    db.collection("career_profiles")
                            .document(latestProfileId)
                            .collection("ai_suggestions")
                            .orderBy("matchScore", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(suggestionsSnapshot -> {
                                latestCareers.clear();
                                Log.d(TAG, "Fetched " + suggestionsSnapshot.size() + " suggestions");

                                for (DocumentSnapshot doc : suggestionsSnapshot.getDocuments()) {
                                    Career career = doc.toObject(Career.class);
                                    if (career != null) {
                                        // Set the Firestore document ID as suggestionId
                                        career.setSuggestionId(doc.getId());
                                        latestCareers.add(career);
                                    }
                                }

                                latestAdapter.notifyDataSetChanged();
                                progressLatest.setVisibility(View.GONE);
                                swipeRefreshLayout.setRefreshing(false);

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
                                Log.e(TAG, "Failed to fetch ai_suggestions", e);
                                progressLatest.setVisibility(View.GONE);
                                emptyLatest.setVisibility(View.VISIBLE);
                                rvLatestCareers.setVisibility(View.GONE);
                                swipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(this, "Could not load career suggestions.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch career profiles", e);
                    progressLatest.setVisibility(View.GONE);
                    emptyLatest.setVisibility(View.VISIBLE);
                    rvLatestCareers.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
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
                        // Map saved career fields back to Career model
                        Career career = new Career();
                        career.setSuggestionId(doc.getString("suggestionId"));
                        career.setCareerName(doc.getString("careerTitle"));
                        career.setDescription(doc.getString("description"));
                        Long score = doc.getLong("matchScore");
                        career.setMatchScore(score != null ? score.intValue() : 0);
                        savedCareers.add(career);
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

    // Fetch certifications in progress from "users/{uid}/certification_progress"
    private void fetchCertificationsProgress() {
        progressCerts.setVisibility(View.VISIBLE);
        emptyCerts.setVisibility(View.GONE);

        db.collection("users").document(uid)
                .collection("certification_progress")
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    certProgressList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Build CertificationProgress from progress data
                        String certName = doc.getString("certificationName");
                        String provider = doc.getString("provider");
                        String level    = doc.getString("level");

                        // Calculate progress percentage
                        Long totalModules = doc.getLong("totalModules");
                        List<?> completedModules = (List<?>) doc.get("completedModules");
                        int total     = totalModules != null ? totalModules.intValue() : 5;
                        int completed = completedModules != null ? completedModules.size() : 0;
                        int percent   = total > 0
                                ? (int) ((completed / (float) total) * 100) : 0;

                        if (certName != null) {
                            CertificationProgress cert = new CertificationProgress();
                            cert.setCertName(certName);
                            cert.setProvider(provider != null ? provider : "");
                            cert.setProgressPercent(percent);
                            certProgressList.add(cert);
                        }
                    }

                    // Also check for docs without "completed" field (treated as in-progress)
                    db.collection("users").document(uid)
                            .collection("certification_progress")
                            .get()
                            .addOnSuccessListener(allDocs -> {
                                for (DocumentSnapshot doc : allDocs.getDocuments()) {
                                    Boolean isCompleted = doc.getBoolean("completed");
                                    // If "completed" field doesn't exist, treat as in-progress
                                    if (isCompleted == null) {
                                        String certName2 = doc.getString("certificationName");
                                        String provider2 = doc.getString("provider");
                                        Long totalModules2 = doc.getLong("totalModules");
                                        List<?> completedModules2 =
                                                (List<?>) doc.get("completedModules");
                                        int total2     = totalModules2 != null
                                                ? totalModules2.intValue() : 5;
                                        int completed2 = completedModules2 != null
                                                ? completedModules2.size() : 0;
                                        int percent2   = total2 > 0
                                                ? (int) ((completed2 / (float) total2) * 100) : 0;

                                        if (certName2 != null) {
                                            // Avoid duplicates
                                            boolean exists = false;
                                            for (CertificationProgress c : certProgressList) {
                                                if (certName2.equals(c.getCertName())) {
                                                    exists = true;
                                                    break;
                                                }
                                            }
                                            if (!exists) {
                                                CertificationProgress cert2 =
                                                        new CertificationProgress();
                                                cert2.setCertName(certName2);
                                                cert2.setProvider(
                                                        provider2 != null ? provider2 : "");
                                                cert2.setProgressPercent(percent2);
                                                certProgressList.add(cert2);
                                            }
                                        }
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
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch certifications progress", e);
                    progressCerts.setVisibility(View.GONE);
                    emptyCerts.setVisibility(View.VISIBLE);
                    rvCertificationsProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not load certifications.",
                            Toast.LENGTH_SHORT).show();
                });
    }
}
