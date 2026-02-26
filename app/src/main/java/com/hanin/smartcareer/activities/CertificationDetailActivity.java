package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.ModuleAdapter;
import com.hanin.smartcareer.models.Module;
import com.hanin.smartcareer.models.OpenAIRequest;
import com.hanin.smartcareer.models.OpenAIResponse;
import com.hanin.smartcareer.models.QuizQuestion;
import com.hanin.smartcareer.network.OpenAIService;
import com.hanin.smartcareer.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CertificationDetailActivity extends AppCompatActivity
        implements ModuleAdapter.OnModuleClickListener {

    private static final String TAG = "CertDetailActivity";
    private static final int MODULE_REQUEST_CODE = 1001;

   

    private ImageView ivBack;
    private TextView tvCertName, tvProvider, tvLevel;
    private TextView tvProgressPercent;
    private ProgressBar progressBar;
    private ProgressBar loadingProgress;
    private Button btnResume;
    private RecyclerView rvModules;

    private List<Module> moduleList;
    private ModuleAdapter moduleAdapter;

    private String certificationName;
    private String provider;
    private String level;
    private int progress;

    private FirebaseFirestore db;
    private String uid;
    private String progressDocId;
    private List<String> savedCompletedModules;
    private Map<String, Long> savedQuizScores;
    private int savedCurrentModuleIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certification_detail);

        // 1. Firebase init
        db  = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        // 2. Retrieve data passed from the previous screen
        retrieveIntentData();

        // 3. Build Firestore document ID from cert name
        progressDocId = certificationName.replaceAll("[^a-zA-Z0-9 ]", "")
                .trim().replaceAll("\\s+", "_");

        // 4. Bind views
        initViews();

        // 5. Populate header with Intent data
        populateHeader();

        // 6. Initialise empty module list
        moduleList = new ArrayList<>();
        savedCompletedModules = new ArrayList<>();
        savedQuizScores = new HashMap<>();
        savedCurrentModuleIndex = 0;

        // 7. Setup RecyclerView with adapter
        setupRecyclerView();

        // 8. Load saved progress from Firestore, then fetch modules
        loadProgressFromFirestore();
    }


    /**
     * Extracts all data passed from the calling activity.
     */
    private void retrieveIntentData() {
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
        btnResume         = findViewById(R.id.btnResume);
        rvModules         = findViewById(R.id.rvModules);

        // Back arrow navigation
        ivBack.setOnClickListener(v -> finish());

        // Resume Learning button → scrolls to and opens first incomplete module
        btnResume.setOnClickListener(v -> resumeLearning());
    }

    // ── Header population ────────────────────────────────────────────

    private void populateHeader() {
        tvCertName.setText(certificationName);
        tvProvider.setText(getString(R.string.cert_detail_provider_prefix, provider));
        tvLevel.setText(level);
        updateProgressUI(progress);
    }

    // ── RecyclerView setup ───────────────────────────────────────────

    private void setupRecyclerView() {
        moduleAdapter = new ModuleAdapter(this, moduleList, this);
        rvModules.setLayoutManager(new LinearLayoutManager(this));
        rvModules.setAdapter(moduleAdapter);
    }

    // MODULE CLICK CALLBACK

    @Override
    public void onModuleClicked(int position) {
        if (position < 0 || position >= moduleList.size()) return;

        Module module = moduleList.get(position);
        if (module.isLocked()) return; // Safety check

        Intent intent = new Intent(this, ModuleActivity.class);
        intent.putExtra("module_data", module);
        intent.putExtra("certification_name", certificationName);
        startActivityForResult(intent, MODULE_REQUEST_CODE);
    }

    // QUIZ RESULT HANDLING

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MODULE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            boolean passed  = data.getBooleanExtra("quiz_passed", false);
            int score       = data.getIntExtra("quiz_score", 0);
            String moduleId = data.getStringExtra("module_id");

            if (passed && moduleId != null) {
                // Find the completed module and update its state
                for (int i = 0; i < moduleList.size(); i++) {
                    Module m = moduleList.get(i);
                    if (m.getModuleId().equals(moduleId)) {
                        // Mark as completed
                        m.setCompleted(true);
                        m.setQuizPassed(true);
                        m.setQuizScore(score);

                        // Unlock next module if it exists
                        if (i + 1 < moduleList.size()) {
                            moduleList.get(i + 1).setLocked(false);
                        }

                        break;
                    }
                }

                // Refresh the adapter
                moduleAdapter.notifyDataSetChanged();

                // Recalculate and update progress
                int newProgress = calculateProgress();
                updateProgressUI(newProgress);

                // Update resume button visibility
                updateResumeButton();

                // Save progress to Firestore
                saveProgressToFirestore(moduleId, score, newProgress);

                // Check if all modules are completed
                if (newProgress == 100) {
                    // Navigate to certificate screen
                    Intent certIntent = new Intent(this, CertificateActivity.class);
                    certIntent.putExtra("certificationName", certificationName);
                    certIntent.putExtra("provider", provider);
                    startActivity(certIntent);
                }
            }
        }
    }

    // RESUME LEARNING
    private void resumeLearning() {
        for (int i = 0; i < moduleList.size(); i++) {
            Module m = moduleList.get(i);
            if (!m.isCompleted() && !m.isLocked()) {
                // Scroll to the module
                rvModules.smoothScrollToPosition(i);
                // Open it
                onModuleClicked(i);
                return;
            }
        }
    }

    /**
     * Shows the Resume button if there are incomplete modules;
     * hides it otherwise.
     */
    private void updateResumeButton() {
        boolean hasIncomplete = false;
        for (Module m : moduleList) {
            if (!m.isCompleted() && !m.isLocked()) {
                hasIncomplete = true;
                break;
            }
        }
        btnResume.setVisibility(hasIncomplete ? View.VISIBLE : View.GONE);
    }


    /**
     * Calculates progress as: (completedModules / totalModules) × 100
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



    /**
     * Loads the user's saved progress for this certification from Firestore.
     * On completion (or failure), triggers the AI module fetch.
     */
    private void loadProgressFromFirestore() {
        if (uid == null) {
            // No user — just fetch modules without progress
            fetchModulesFromAI();
            return;
        }

        db.collection("users").document(uid)
                .collection("certification_progress").document(progressDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Load completed modules list
                        List<?> rawCompleted = (List<?>) doc.get("completedModules");
                        if (rawCompleted != null) {
                            savedCompletedModules = new ArrayList<>();
                            for (Object o : rawCompleted) {
                                savedCompletedModules.add(String.valueOf(o));
                            }
                        }

                        // Load quiz scores map
                        Map<String, Object> rawScores =
                                (Map<String, Object>) doc.get("quizScores");
                        if (rawScores != null) {
                            savedQuizScores = new HashMap<>();
                            for (Map.Entry<String, Object> entry : rawScores.entrySet()) {
                                if (entry.getValue() instanceof Number) {
                                    savedQuizScores.put(entry.getKey(),
                                            ((Number) entry.getValue()).longValue());
                                }
                            }
                        }

                        // Load current module index for resume
                        Long idx = doc.getLong("currentModuleIndex");
                        if (idx != null) {
                            savedCurrentModuleIndex = idx.intValue();
                        }

                        Log.d(TAG, "Loaded progress: " + savedCompletedModules.size()
                                + " completed modules");
                    }

                    // Now fetch AI modules (they'll have progress applied)
                    fetchModulesFromAI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load progress", e);
                    fetchModulesFromAI();
                });
    }

    /**
     * Saves the current progress state to Firestore after a quiz pass.

     */
    private void saveProgressToFirestore(String completedModuleId,
                                          int quizScore,
                                          int overallProgress) {
        if (uid == null) return;

        // Build the list of all completed module IDs
        List<String> completedIds = new ArrayList<>();
        int currentIdx = 0;
        for (int i = 0; i < moduleList.size(); i++) {
            Module m = moduleList.get(i);
            if (m.isCompleted()) {
                completedIds.add(m.getModuleId());
            } else if (!m.isLocked()) {
                currentIdx = i; // First unlocked, incomplete = resume point
            }
        }

        // Build quiz scores map
        Map<String, Object> quizScoresMap = new HashMap<>();
        for (Module m : moduleList) {
            if (m.isCompleted() && m.getQuizScore() > 0) {
                quizScoresMap.put(m.getModuleId(), m.getQuizScore());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("certificationName", certificationName);
        data.put("provider", provider);
        data.put("level", level);
        data.put("totalModules", moduleList.size());
        data.put("completedModules", completedIds);
        data.put("quizScores", quizScoresMap);
        data.put("currentModuleIndex", currentIdx);
        data.put("completed", overallProgress == 100);

        db.collection("users").document(uid)
                .collection("certification_progress").document(progressDocId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Progress saved successfully"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to save progress", e));
    }


    /**
     * Calls the Groq LLaMA AI API to generate learning modules
     * with content, duration, and quiz questions.
     */
    private void fetchModulesFromAI() {
        // Show loading, hide RecyclerView
        loadingProgress.setVisibility(View.VISIBLE);
        rvModules.setVisibility(View.GONE);
        btnResume.setVisibility(View.GONE);

        // Build the AI request
        OpenAIRequest request = OpenAIRequest.buildModulesPrompt(
                certificationName, provider, level);

        // Make the API call via Retrofit
        OpenAIService service = RetrofitClient.createService(OpenAIService.class);
        service.getChatCompletion(request).enqueue(new Callback<OpenAIResponse>() {

            @Override
            public void onResponse(Call<OpenAIResponse> call,
                                   Response<OpenAIResponse> response) {
                loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    String content = response.body().extractContent();

                    if (content != null && !content.isEmpty()) {
                        parseAndDisplayModules(content);
                    } else {
                        Log.w(TAG, "AI returned empty content");
                        Toast.makeText(CertificationDetailActivity.this,
                                getString(R.string.cert_detail_ai_empty),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    int code = response.code();
                    Log.e(TAG, "API error: HTTP " + code);
                    Toast.makeText(CertificationDetailActivity.this,
                            getString(R.string.cert_detail_ai_error),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                Log.e(TAG, "Network error", t);
                Toast.makeText(CertificationDetailActivity.this,
                        getString(R.string.cert_detail_ai_error),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Parses the AI JSON response into {@link Module} objects, applies
     * saved progress (completed/locked states), and updates the UI.
     *
     */
    private void parseAndDisplayModules(String jsonContent) {
        // First, try a direct parse
        ModuleListWrapper wrapper = null;
        try {
            Gson gson = new Gson();
            wrapper = gson.fromJson(jsonContent, ModuleListWrapper.class);
        } catch (Exception e) {
            Log.w(TAG, "Direct JSON parse failed, attempting truncation repair", e);
            // Attempt to repair truncated JSON
            wrapper = attemptTruncatedJsonRepair(jsonContent);
        }

        if (wrapper != null && wrapper.modules != null && !wrapper.modules.isEmpty()) {
            moduleList.clear();

            for (int i = 0; i < wrapper.modules.size(); i++) {
                ModuleEntry entry = wrapper.modules.get(i);

                // Skip entries with missing essential data
                if (entry.id == null || entry.title == null) continue;

                // Convert quiz entries to QuizQuestion objects
                ArrayList<QuizQuestion> quizQuestions = new ArrayList<>();
                if (entry.quiz != null) {
                    for (QuizEntry qe : entry.quiz) {
                        // Only add complete quiz questions
                        if (qe.question != null && qe.options != null
                                && qe.options.size() == 4) {
                            quizQuestions.add(new QuizQuestion(
                                    qe.question, qe.options, qe.correctIndex));
                        }
                    }
                }

                // Create Module with all fields
                Module module = new Module(
                        entry.id,
                        entry.title,
                        entry.content != null ? entry.content : "",
                        entry.duration != null ? entry.duration : "20 min",
                        i,
                        quizQuestions);

                moduleList.add(module);
            }

            if (!moduleList.isEmpty()) {
                // Apply saved progress
                applyProgressToModules();

                // Show the RecyclerView and refresh
                rvModules.setVisibility(View.VISIBLE);
                moduleAdapter.notifyDataSetChanged();

                // Update progress UI
                int newProgress = calculateProgress();
                updateProgressUI(newProgress);

                // Show/hide resume button
                updateResumeButton();

                Log.d(TAG, "Successfully loaded " + moduleList.size() + " modules");
            } else {
                Log.w(TAG, "No valid modules after filtering");
                Toast.makeText(this,
                        getString(R.string.cert_detail_ai_empty),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Parsed module list is empty");
            Toast.makeText(this,
                    getString(R.string.cert_detail_ai_empty),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Attempts to repair truncated JSON from a cut-off AI response.
     *
     */
    private ModuleListWrapper attemptTruncatedJsonRepair(String truncatedJson) {
        try {
            // Find the last occurrence of a complete module boundary
            // Each module ends with "}" followed by optional whitespace and ","
            // We look for the pattern "]" closing quiz array + "}" closing module
            int lastCompleteModule = -1;

            // Scan for complete module entries by tracking brace depth
            // A simpler approach: find each "mod_N" and check if it has a
            // complete quiz array (ends with "]}")
            for (int modNum = 5; modNum >= 1; modNum--) {
                String marker = "\"id\": \"mod_" + modNum + "\"";
                // Also try without space after colon
                String marker2 = "\"id\":\"mod_" + modNum + "\"";

                int pos = truncatedJson.lastIndexOf(marker);
                if (pos == -1) pos = truncatedJson.lastIndexOf(marker2);
                if (pos == -1) continue;

                // Find the closing of this module's quiz array: "]\n    }"
                // Search for "]" after the quiz starts, then "}" after it
                String afterMarker = truncatedJson.substring(pos);
                int quizEnd = findLastCompleteQuizEnd(afterMarker);

                if (quizEnd > 0) {
                    // Found a complete module — trim here
                    lastCompleteModule = pos + quizEnd;
                    break;
                }
            }

            if (lastCompleteModule > 0) {
                // Trim to the last complete module and close the JSON
                String repaired = truncatedJson.substring(0, lastCompleteModule)
                        + "\n  ]\n}";
                Log.d(TAG, "Repaired JSON, attempting re-parse");
                return new Gson().fromJson(repaired, ModuleListWrapper.class);
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON repair also failed", e);
        }
        return null;
    }

    /**
     * Finds the end position of the last complete quiz array + module closing
     * brace in the given string fragment.
     */
    private int findLastCompleteQuizEnd(String fragment) {
        // Look for the quiz array closing pattern: ] followed by }
        int searchFrom = 0;
        int lastValid = -1;

        while (true) {
            int bracketClose = fragment.indexOf("]", searchFrom);
            if (bracketClose == -1) break;

            // Look for the module closing brace after the bracket
            int braceClose = -1;
            for (int i = bracketClose + 1; i < fragment.length(); i++) {
                char c = fragment.charAt(i);
                if (c == '}') {
                    braceClose = i;
                    break;
                } else if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                    break; // Non-whitespace before } means this isn't the right ]
                }
            }

            if (braceClose != -1) {
                lastValid = braceClose + 1;
            }

            searchFrom = bracketClose + 1;
        }

        return lastValid;
    }

    /**
     * Applies saved progress data to the parsed modules.
     * Marks completed modules, sets quiz scores, and unlocks
     * the next available module.
     */
    private void applyProgressToModules() {
        if (savedCompletedModules == null || savedCompletedModules.isEmpty()) return;

        for (Module m : moduleList) {
            if (savedCompletedModules.contains(m.getModuleId())) {
                m.setCompleted(true);
                m.setQuizPassed(true);
                m.setLocked(false);

                // Restore quiz score
                if (savedQuizScores != null
                        && savedQuizScores.containsKey(m.getModuleId())) {
                    m.setQuizScore(savedQuizScores.get(m.getModuleId()).intValue());
                }
            }
        }

        // Unlock the first incomplete module after the last completed one
        for (int i = 0; i < moduleList.size(); i++) {
            Module m = moduleList.get(i);
            if (!m.isCompleted()) {
                // Check if previous module is completed (or it's the first)
                if (i == 0 || moduleList.get(i - 1).isCompleted()) {
                    m.setLocked(false);
                }
                break; // Only unlock the next one
            }
        }
    }

    // ── JSON wrapper classes for AI module response ──────────────────

    /**
     * Top-level wrapper for the AI's JSON response.
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

        @SerializedName("content")
        String content;

        @SerializedName("duration")
        String duration;

        @SerializedName("quiz")
        List<QuizEntry> quiz;
    }

    /**
     * A single quiz question entry as returned by the AI.
     */
    private static class QuizEntry {
        @SerializedName("question")
        String question;

        @SerializedName("options")
        ArrayList<String> options;

        @SerializedName("correctIndex")
        int correctIndex;
    }
}
