package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CertificateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        String certName = getIntent().getStringExtra("certificationName");
        String provider = getIntent().getStringExtra("provider");

        if (certName == null) certName = "Certification";
        if (provider == null) provider = "Provider";

        TextView tvCertName = findViewById(R.id.tvCertName);
        TextView tvProvider = findViewById(R.id.tvProvider);
        TextView tvDate     = findViewById(R.id.tvDate);
        Button btnBackHome  = findViewById(R.id.btnBackHome);

        tvCertName.setText(certName);
        tvProvider.setText(getString(R.string.cert_provider_label, provider));

        String dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date());
        tvDate.setText(getString(R.string.cert_date_label, dateStr));

        saveCompletionRecord(certName, provider);

   
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // ── Firestore: save completion record ───────────────────────────

 
    private void saveCompletionRecord(String certName, String provider) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null) return;

        // Sanitize cert name for use as Firestore document ID
        String docId = certName.replaceAll("[^a-zA-Z0-9 ]", "")
                .trim().replaceAll("\\s+", "_");

        Map<String, Object> data = new HashMap<>();
        data.put("completed", true);
        data.put("completedAt", FieldValue.serverTimestamp());
        data.put("certificationName", certName);
        data.put("provider", provider);

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("certification_progress").document(docId)
                .set(data, com.google.firebase.firestore.SetOptions.merge());
    }
}
