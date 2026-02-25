package com.hanin.smartcareer.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.ModuleAdapter;
import com.hanin.smartcareer.models.Module;
import com.hanin.smartcareer.models.OpenAIRequest;
import com.hanin.smartcareer.models.OpenAIResponse;
import com.hanin.smartcareer.network.OpenAIService;
import com.hanin.smartcareer.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays the full detail of a selected certification, including
 * overall progress and a list of AI-generated learning modules.
 *
 * <p>When the screen opens, it calls the Groq LLaMA AI API to generate
 * relevant learning modules for the selected certification. The student
 * can then mark individual modules as completed and the overall progress
 * is recalculated dynamically.</p>
 *
 * <p>Data is received from the previous screen via Intent extras.</p>
 */
public class CertificationDetailActivity extends AppCompatActivity
        implements ModuleAdapter.OnModuleCompletedListener {

    private static final String TAG = "CertDetailActivity";

    // ── View references ─────────────────────────────────────────────

    private ImageView ivBack;
    private TextView tvCertName, tvProvider, tvLevel;
    private TextView tvProgressPercent;
    private ProgressBar progressBar;
    private ProgressBar loadingProgress;
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

        // 4. Initialise empty module list
        moduleList = new ArrayList<>();

        // 5. Setup RecyclerView with adapter
        setupRecyclerView();

        // 6. Fetch AI-generated modules from the Groq LLaMA API
        fetchModulesFromAI();
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
        loadingProgress   = findViewById(R.id.loadingProgress);
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

    // ══════════════════════════════════════════════════════════════════
    // AI-POWERED MODULE GENERATION
    // ══════════════════════════════════════════════════════════════════

    /**
     * Calls the Groq LLaMA AI API to generate learning modules
     * tailored to the current certification.
     *
     * <p>While the request is in flight, a loading spinner is shown.
     * On success, the JSON response is parsed into {@link Module} objects
     * and displayed in the RecyclerView. On failure, an error Toast is shown.</p>
     */
    private void fetchModulesFromAI() {
        // Show loading, hide RecyclerView
        loadingProgress.setVisibility(View.VISIBLE);
        rvModules.setVisibility(View.GONE);

        // Build the AI request using the certification details
        OpenAIRequest request = OpenAIRequest.buildModulesPrompt(
                certificationName, provider, level);

        // Make the API call via Retrofit
        OpenAIService service = RetrofitClient.createService(OpenAIService.class);
        service.getChatCompletion(request).enqueue(new Callback<OpenAIResponse>() {

            @Override
            public void onResponse(Call<OpenAIResponse> call,
                                   Response<OpenAIResponse> response) {
                // Hide loading spinner
                loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    String content = response.body().extractContent();

                    if (content != null && !content.isEmpty()) {
                        // Parse the AI-generated JSON into Module objects
                        parseAndDisplayModules(content);
                    } else {
                        // Empty response from AI
                        Log.w(TAG, "AI returned empty content");
                        Toast.makeText(CertificationDetailActivity.this,
                                getString(R.string.cert_detail_ai_empty),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error
                    int code = response.code();
                    Log.e(TAG, "API error: HTTP " + code);
                    Toast.makeText(CertificationDetailActivity.this,
                            getString(R.string.cert_detail_ai_error),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                // Hide loading spinner
                loadingProgress.setVisibility(View.GONE);

                Log.e(TAG, "Network error", t);
                Toast.makeText(CertificationDetailActivity.this,
                        getString(R.string.cert_detail_ai_error),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Parses the raw JSON string from the AI response into a list of
     * {@link Module} objects and updates the RecyclerView.
     *
     * <p>Expected JSON format:
     * <pre>
     * {
     *   "modules": [
     *     { "id": "mod_1", "title": "Module 1: ..." },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param jsonContent The raw JSON string from the AI.
     */
    private void parseAndDisplayModules(String jsonContent) {
        try {
            Gson gson = new Gson();
            ModuleListWrapper wrapper = gson.fromJson(jsonContent, ModuleListWrapper.class);

            if (wrapper != null && wrapper.modules != null && !wrapper.modules.isEmpty()) {
                // Convert parsed entries into Module model objects
                moduleList.clear();
                for (ModuleEntry entry : wrapper.modules) {
                    moduleList.add(new Module(entry.id, entry.title, false));
                }

                // Show the RecyclerView and refresh the adapter
                rvModules.setVisibility(View.VISIBLE);
                moduleAdapter.notifyDataSetChanged();

                // Reset progress to 0 since all modules are fresh
                updateProgressUI(0);
            } else {
                Log.w(TAG, "Parsed module list is empty");
                Toast.makeText(this,
                        getString(R.string.cert_detail_ai_empty),
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON parse error", e);
            Toast.makeText(this,
                    getString(R.string.cert_detail_ai_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── JSON wrapper for AI module response ──────────────────────────

    /**
     * Wrapper for deserialising the AI's JSON response into a list
     * of module entries using Gson.
     */
    private static class ModuleListWrapper {
        @SerializedName("modules")
        List<ModuleEntry> modules;
    }

    /**
     * A single module entry as returned by the AI.
     */
    private static class ModuleEntry {
        @SerializedName("id")
        String id;

        @SerializedName("title")
        String title;
    }

    // ══════════════════════════════════════════════════════════════════
    // FIREBASE INTEGRATION PLACEHOLDERS
    // ══════════════════════════════════════════════════════════════════

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
