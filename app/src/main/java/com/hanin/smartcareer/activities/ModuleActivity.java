package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Module;

/**
 * Displays the full content of a single learning module.
 *
 * <p>Shows the module title, estimated duration, and a scrollable
 * lesson body. A "Take Quiz" button at the bottom launches
 * {@link QuizActivity} for assessment.</p>
 *
 * <p>Data is received via Intent as a {@link Module} Serializable object.</p>
 */
public class ModuleActivity extends AppCompatActivity {

    // Request code for quiz result
    public static final int QUIZ_REQUEST_CODE = 2001;

    private Module module;

    // ── Lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        // 1. Retrieve module data from intent
        module = (Module) getIntent().getSerializableExtra("module_data");

        // 2. Bind views
        ImageView ivBack     = findViewById(R.id.ivBack);
        TextView tvTitle     = findViewById(R.id.tvModuleTitle);
        TextView tvDuration  = findViewById(R.id.tvDuration);
        TextView tvContent   = findViewById(R.id.tvContent);
        Button btnTakeQuiz   = findViewById(R.id.btnTakeQuiz);

        // 3. Populate UI
        if (module != null) {
            tvTitle.setText(module.getTitle());

            if (module.getDuration() != null && !module.getDuration().isEmpty()) {
                tvDuration.setText(getString(R.string.module_duration_prefix,
                        module.getDuration()));
            }

            if (module.getContent() != null) {
                tvContent.setText(module.getContent());
            }
        }

        // 4. Back arrow → finish activity
        ivBack.setOnClickListener(v -> finish());

        // 5. Take Quiz → launch QuizActivity
        btnTakeQuiz.setOnClickListener(v -> {
            if (module != null && module.getQuizQuestions() != null
                    && !module.getQuizQuestions().isEmpty()) {

                Intent intent = new Intent(ModuleActivity.this, QuizActivity.class);
                intent.putExtra("module_data", module);
                startActivityForResult(intent, QUIZ_REQUEST_CODE);
            }
        });
    }

    // ── Handle quiz result ──────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QUIZ_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Pass the quiz result back to CertificationDetailActivity
            boolean passed = data.getBooleanExtra("quiz_passed", false);
            int score      = data.getIntExtra("quiz_score", 0);
            String moduleId = data.getStringExtra("module_id");

            Intent resultIntent = new Intent();
            resultIntent.putExtra("quiz_passed", passed);
            resultIntent.putExtra("quiz_score", score);
            resultIntent.putExtra("module_id", moduleId);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
