package com.hanin.smartcareer.activities;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboardActivity";

    private TextView tvAdminWelcome;
    private ProgressBar progressStats;
    private GridLayout gridStats;

    private TextView tvStatUsers;
    private TextView tvStatProfiles;
    private TextView tvStatCertsStarted;
    private TextView tvStatCertsCompleted;

    private MaterialButton btnManageUsers;
    private MaterialButton btnManageCerts;
    private MaterialButton btnViewLogs;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    
    private int completedQueries = 0;
    private static final int TOTAL_QUERIES = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialise Firebase instances
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // ── Null-user check ─────────────────────────────────────────
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();

        String adminName = "Admin";
        if (currentUser.getDisplayName() != null
                && !currentUser.getDisplayName().isEmpty()) {
            adminName = currentUser.getDisplayName();
        }
        tvAdminWelcome.setText(getString(R.string.admin_welcome, adminName));

        // ── Role verification (SECURITY) ────────────────────────────
        verifyAdminRole(currentUser.getUid());

        // Set up button click listeners
        setupActionButtons();
    }

    // ── View binding ────────────────────────────────────────────────

    private void initViews() {
        tvAdminWelcome     = findViewById(R.id.tvAdminWelcome);
        progressStats      = findViewById(R.id.progressStats);
        gridStats          = findViewById(R.id.gridStats);
        tvStatUsers        = findViewById(R.id.tvStatUsers);
        tvStatProfiles     = findViewById(R.id.tvStatProfiles);
        tvStatCertsStarted = findViewById(R.id.tvStatCertsStarted);
        tvStatCertsCompleted = findViewById(R.id.tvStatCertsCompleted);
        btnManageUsers     = findViewById(R.id.btnManageUsers);
        btnManageCerts     = findViewById(R.id.btnManageCerts);
        btnViewLogs        = findViewById(R.id.btnViewLogs);

        // Logout icon in the header
        ImageView ivLogout = findViewById(R.id.ivAdminLogout);
        ivLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    // ── Role verification ───────────────────────────────────────────


    private void verifyAdminRole(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Read the role field from the document
                        String role = snapshot.getString("role");

                        if ("admin".equals(role)) {
                            // ✅ Authorized — fetch dashboard statistics
                            Log.d(TAG, "Admin role verified for UID: " + uid);
                            fetchAllStats();
                        } else {
                            // ❌ Not an admin — redirect to HomeActivity
                            Log.w(TAG, "Unauthorized role: " + role);
                            Toast.makeText(this,
                                    R.string.admin_unauthorized,
                                    Toast.LENGTH_SHORT).show();
                            redirectToHome();
                        }
                    } else {
                        Log.w(TAG, "User document not found for UID: " + uid);
                        Toast.makeText(this,
                                R.string.admin_unauthorized,
                                Toast.LENGTH_SHORT).show();
                        redirectToHome();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to verify admin role", e);
                    Toast.makeText(this,
                            R.string.admin_unauthorized,
                            Toast.LENGTH_SHORT).show();
                    redirectToHome();
                });
    }

    // ── Statistics fetching ─────────────────────────────────────────

    private void onStatQueryComplete() {
        completedQueries++;
        Log.d(TAG, "Stat query completed: " + completedQueries + "/" + TOTAL_QUERIES);
        if (completedQueries >= TOTAL_QUERIES) {
            // All queries have returned — show the stat grid
            progressStats.setVisibility(View.GONE);
            gridStats.setVisibility(View.VISIBLE);
            Log.d(TAG, "All stat queries complete — showing grid");
        }
    }

    private void fetchAllStats() {
        // Reset the completion counter
        completedQueries = 0;

        // Show the loading spinner and hide the stat grid
        progressStats.setVisibility(View.VISIBLE);
        gridStats.setVisibility(View.GONE);

        // STAT 1: Total Users — count all documents in "users" collection
        db.collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {
                    tvStatUsers.setText(String.valueOf(snapshot.size()));
                    Log.d(TAG, "Total users: " + snapshot.size());
                    onStatQueryComplete();
                })
                .addOnFailureListener(e -> {
                    tvStatUsers.setText("0");
                    Log.e(TAG, "Failed to count users", e);
                    onStatQueryComplete();
                });

        // STAT 2: Total Career Profiles — count "career_profiles" docs
        db.collection("career_profiles")
                .get()
                .addOnSuccessListener(snapshot -> {
                    tvStatProfiles.setText(String.valueOf(snapshot.size()));
                    Log.d(TAG, "Total career profiles: " + snapshot.size());
                    onStatQueryComplete();
                })
                .addOnFailureListener(e -> {
                    tvStatProfiles.setText("0");
                    Log.e(TAG, "Failed to count career profiles", e);
                    onStatQueryComplete();
                });

        // STAT 3 & 4: Certification stats — iterate over each user's
        fetchCertificationStats();
    }

    
    private void fetchCertificationStats() {
        // Step 1: Get all user IDs
        db.collection("users")
                .get()
                .addOnSuccessListener(usersSnapshot -> {
                    // If there are no users, show 0/0 and mark complete
                    if (usersSnapshot.isEmpty()) {
                        tvStatCertsStarted.setText("0");
                        tvStatCertsCompleted.setText("0");
                        Log.d(TAG, "No users found for cert stats");
                        onStatQueryComplete();
                        return;
                    }

                    // Track counts across all users using arrays (mutable in lambdas)
                    final int[] totalStarted   = {0};
                    final int[] totalCompleted  = {0};
                    final int[] usersProcessed  = {0};
                    final int totalUsers = usersSnapshot.size();

                    Log.d(TAG, "Fetching cert stats for " + totalUsers + " users");

                    // Step 2: For each user, query their certification_progress
                    for (DocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
                        String userId = userDoc.getId();

                        db.collection("users")
                                .document(userId)
                                .collection("certification_progress")
                                .get()
                                .addOnSuccessListener(certsSnapshot -> {
                                    // Count completed vs in-progress for this user
                                    for (DocumentSnapshot doc : certsSnapshot.getDocuments()) {
                                        Boolean completed = doc.getBoolean("completed");
                                        if (completed != null && completed) {
                                            totalCompleted[0]++;
                                        } else {
                                            totalStarted[0]++;
                                        }
                                    }

                                    // Check if all users have been processed
                                    usersProcessed[0]++;
                                    if (usersProcessed[0] >= totalUsers) {
                                        // All users processed — update the UI
                                        tvStatCertsStarted.setText(
                                                String.valueOf(totalStarted[0]));
                                        tvStatCertsCompleted.setText(
                                                String.valueOf(totalCompleted[0]));
                                        Log.d(TAG, "Certs started: " + totalStarted[0]
                                                + ", completed: " + totalCompleted[0]);
                                        onStatQueryComplete();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to read certs for user: "
                                            + userId, e);                            
                                    usersProcessed[0]++;
                                    if (usersProcessed[0] >= totalUsers) {
                                        tvStatCertsStarted.setText(
                                                String.valueOf(totalStarted[0]));
                                        tvStatCertsCompleted.setText(
                                                String.valueOf(totalCompleted[0]));
                                        onStatQueryComplete();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    tvStatCertsStarted.setText("0");
                    tvStatCertsCompleted.setText("0");
                    Log.e(TAG, "Failed to fetch users for cert stats", e);
                    onStatQueryComplete();

                    Toast.makeText(this,
                            R.string.admin_stats_error,
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Action buttons ──────────────────────────────────────────────

    private void setupActionButtons() {
        // Manage Users → UsersListActivity
        btnManageUsers.setOnClickListener(v ->
                startActivity(new Intent(this, UsersListActivity.class)));

        // Manage Certifications → CertificationsAdminActivity
        btnManageCerts.setOnClickListener(v ->
                startActivity(new Intent(this, CertificationsAdminActivity.class)));

        // View Activity Logs → ActivityLogsActivity
        btnViewLogs.setOnClickListener(v ->
                startActivity(new Intent(this, ActivityLogsActivity.class)));
    }

    // ── Logout ──────────────────────────────────────────────────────

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.admin_logout))
                .setMessage(getString(R.string.admin_logout_confirm))
                .setPositiveButton(getString(R.string.admin_logout), (dialog, which) -> {
                    // Sign out and navigate to LoginActivity
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.admin_logout_cancel), null)
                .show();
    }

    // ── Navigation helpers ─────────────────────────────────────────
    private void redirectToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
