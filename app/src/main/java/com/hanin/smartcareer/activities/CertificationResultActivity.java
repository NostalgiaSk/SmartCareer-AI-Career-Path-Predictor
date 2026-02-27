package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hanin.smartcareer.R;

public class CertificationResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certification_result);

        // Extract intent data
        String certTitle    = getIntent().getStringExtra("certTitle");
        String completionDate = getIntent().getStringExtra("completionDate");
        int finalScore      = getIntent().getIntExtra("finalScore", 0);
        String provider     = getIntent().getStringExtra("provider");
        int totalModules    = getIntent().getIntExtra("totalModules", 0);

        // Null-safe defaults
        if (certTitle == null) certTitle = "Certification";
        if (completionDate == null) completionDate = "—";
        if (provider == null) provider = "—";

        // Bind views
        ImageView ivBack        = findViewById(R.id.ivBack);
        TextView tvCertTitle    = findViewById(R.id.tvCertTitle);
        TextView tvFinalScore   = findViewById(R.id.tvFinalScore);
        TextView tvCompletionDate = findViewById(R.id.tvCompletionDate);
        TextView tvModulesCompleted = findViewById(R.id.tvModulesCompleted);
        TextView tvProvider     = findViewById(R.id.tvProvider);
        Button btnBackHome      = findViewById(R.id.btnBackHome);

        // Populate UI
        tvCertTitle.setText(certTitle);
        tvFinalScore.setText(finalScore + "%");
        tvCompletionDate.setText(completionDate);
        tvModulesCompleted.setText(totalModules + " / " + totalModules);
        tvProvider.setText(provider);

        // Back arrow
        ivBack.setOnClickListener(v -> finish());

        // Back to Home
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
