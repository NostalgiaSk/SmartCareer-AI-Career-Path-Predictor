package com.hanin.smartcareer.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.ModuleAdapter;
import com.hanin.smartcareer.models.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the full detail of a selected certification, including
 * overall progress and a list of learning modules.
 *
 * <p>When the student marks individual modules as completed, the
 * overall progress is recalculated dynamically and the ProgressBar
 * updates in real-time. Once all modules are done a congratulatory
 * Toast is shown.</p>
 *
 * <p>Data is received from the previous screen via Intent extras.</p>
 */
public class CertificationDetailActivity extends AppCompatActivity
        implements ModuleAdapter.OnModuleCompletedListener {

    // ── View references ─────────────────────────────────────────────

    private ImageView ivBack;
    private TextView tvCertName, tvProvider, tvLevel;
    private TextView tvProgressPercent;
    private ProgressBar progressBar;
    private RecyclerView rvModules;

    // ── Data ─────────────────────────────────────────────────────────

    private List<Module> moduleList;
    private ModuleAdapter moduleAdapter;

    // Intent extras
    private String certificationId;
    private String certificationName;
    private String provider;
    private String level;
    private int progress;

    // ── Lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certification_detail);

        // 1. Retrieve data passed from the previous screen
        retrieveIntentData();

        // 2. Bind views
        initViews();

        // 3. Populate header with Intent data
        populateHeader();

        // 4. Load modules (dummy data for now)
        loadDummyModules();

        // 5. Setup RecyclerView with adapter
        setupRecyclerView();

        // 6. Firebase placeholder — swap with live data
        fetchCertificationModulesFromFirestore();
    }

    // ── Intent data retrieval ────────────────────────────────────────

    /**
     * Extracts all data passed from the calling activity.
     * Expected extras: certificationId, certificationName, provider,
     * level, progress.
     */
    private void retrieveIntentData() {
        certificationId   = getIntent().getStringExtra("certificationId");
        certificationName = getIntent().getStringExtra("certificationName");
        provider          = getIntent().getStringExtra("provider");
        level             = getIntent().getStringExtra("level");
        progress          = getIntent().getIntExtra("progress", 0);

        // Fallbacks for standalone testing
        if (certificationName == null) certificationName = "Google ML Engineer";
        if (provider == null)          provider = "Google";
        if (level == null)             level = "Intermediate";
    }

    // ── View initialisation ──────────────────────────────────────────

    private void initViews() {
        ivBack            = findViewById(R.id.ivBack);
        tvCertName        = findViewById(R.id.tvCertName);
        tvProvider        = findViewById(R.id.tvProvider);
        tvLevel           = findViewById(R.id.tvLevel);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        progressBar       = findViewById(R.id.progressBar);
        rvModules         = findViewById(R.id.rvModules);

        // Back arrow navigation
        ivBack.setOnClickListener(v -> finish());
    }

    // ── Header population ────────────────────────────────────────────

    private void populateHeader() {
        tvCertName.setText(certificationName);
        tvProvider.setText(getString(R.string.cert_detail_provider_prefix, provider));
        tvLevel.setText(level);
        updateProgressUI(progress);
    }

    // ── RecyclerView setup ───────────────────────────────────────────

    /**
     * Configures the modules RecyclerView with a vertical
     * LinearLayoutManager and attaches the adapter.
     */
    private void setupRecyclerView() {
        moduleAdapter = new ModuleAdapter(this, moduleList, this);
        rvModules.setLayoutManager(new LinearLayoutManager(this));
        rvModules.setAdapter(moduleAdapter);
    }

    // ── Module completion callback ───────────────────────────────────

    /**
     * Called by {@link ModuleAdapter} when the student taps
     * "Mark as Completed" on a module.
     *
     * @param position The adapter position of the completed module.
     */
    @Override
    public void onModuleCompleted(int position) {
        // 1. Update the module's state
        Module module = moduleList.get(position);
        module.setCompleted(true);

        // 2. Refresh that single item in the list
        moduleAdapter.notifyItemChanged(position);

        // 3. Recalculate overall progress
        int newProgress = calculateProgress();
        updateProgressUI(newProgress);

        // 4. Persist to Firestore (placeholder)
        updateModuleCompletionInFirestore(module.getModuleId());
        updateCertificationProgressInFirestore(newProgress);

        // 5. Check if all modules are done
        if (newProgress == 100) {
            Toast.makeText(this,
                    getString(R.string.cert_detail_all_complete),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── Progress calculation ─────────────────────────────────────────

    /**
     * Calculates progress as:  (completedModules / totalModules) × 100
     *
     * @return Progress percentage as an integer (0 – 100).
     */
    private int calculateProgress() {
        if (moduleList == null || moduleList.isEmpty()) return 0;

        int completed = 0;
        for (Module m : moduleList) {
            if (m.isCompleted()) completed++;
        }
        return (int) ((completed / (float) moduleList.size()) * 100);
    }

    /**
     * Updates both the percentage text and the ProgressBar.
     */
    private void updateProgressUI(int percent) {
        tvProgressPercent.setText(getString(R.string.cert_detail_progress_text, percent));
        progressBar.setProgress(percent);
    }

    // ── Dummy data ───────────────────────────────────────────────────

    private void loadDummyModules() {
        moduleList = new ArrayList<>();
        moduleList.add(new Module("mod_1", "Module 1: Introduction to Machine Learning", false));
        moduleList.add(new Module("mod_2", "Module 2: Supervised Learning Algorithms", false));
        moduleList.add(new Module("mod_3", "Module 3: Neural Networks & Deep Learning", false));
        moduleList.add(new Module("mod_4", "Module 4: Model Deployment & MLOps", false));
        moduleList.add(new Module("mod_5", "Module 5: Final Project & Assessment", false));
    }

    // ══════════════════════════════════════════════════════════════════
    // FIREBASE INTEGRATION PLACEHOLDERS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Placeholder — fetch the list of modules for this certification
     * from Firestore.
     *
     * Implementation:
     * 1. db.collection("certifications").document(certificationId)
     *        .collection("modules")
     *        .orderBy("order")
     *        .get()
     * 2. Parse documents into Module objects
     * 3. Replace moduleList contents and call adapter.notifyDataSetChanged()
     */
    private void fetchCertificationModulesFromFirestore() {
        // TODO: Implement Firestore query
    }

    /**
     * Placeholder — mark a specific module as completed in Firestore.
     *
     * Implementation:
     * 1. db.collection("certifications").document(certificationId)
     *        .collection("modules").document(moduleId)
     *        .update("completed", true)
     *
     * @param moduleId The Firestore document ID of the completed module.
     */
    private void updateModuleCompletionInFirestore(String moduleId) {
        // TODO: Implement Firestore update
    }

    /**
     * Placeholder — update the overall progress percentage in Firestore.
     *
     * Implementation:
     * 1. db.collection("certifications_progress").document(certificationId)
     *        .update("progress", newProgress)
     *
     * @param newProgress The recalculated progress percentage (0 – 100).
     */
    private void updateCertificationProgressInFirestore(int newProgress) {
        // TODO: Implement Firestore update
    }
}
