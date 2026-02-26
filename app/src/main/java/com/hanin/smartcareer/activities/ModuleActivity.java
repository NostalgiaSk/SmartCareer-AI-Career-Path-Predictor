package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Module;


public class ModuleActivity extends AppCompatActivity {

    // Request code for quiz result
    public static final int QUIZ_REQUEST_CODE = 2001;

    private Module module;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

       
        module = (Module) getIntent().getSerializableExtra("module_data");

        ImageView ivBack     = findViewById(R.id.ivBack);
        TextView tvTitle     = findViewById(R.id.tvModuleTitle);
        TextView tvDuration  = findViewById(R.id.tvDuration);
        TextView tvContent   = findViewById(R.id.tvContent);
        Button btnTakeQuiz   = findViewById(R.id.btnTakeQuiz);

        
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


        ivBack.setOnClickListener(v -> finish());

       
        btnTakeQuiz.setOnClickListener(v -> {
            if (module != null && module.getQuizQuestions() != null
                    && !module.getQuizQuestions().isEmpty()) {

                Intent intent = new Intent(ModuleActivity.this, QuizActivity.class);
                intent.putExtra("module_data", module);
                // Forward certification name for Firestore attempt tracking
                intent.putExtra("certification_name",
                        getIntent().getStringExtra("certification_name"));
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
