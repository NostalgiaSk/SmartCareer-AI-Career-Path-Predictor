package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.ObtainedCertificationsAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObtainedCertificationsActivity extends AppCompatActivity {

    private static final String TAG = "ObtainedCerts";

    private RecyclerView rvObtainedCerts;
    private ProgressBar progressLoading;
    private LinearLayout layoutEmpty;
    private Button btnStartLearning;

    private List<Map<String, Object>> certList;
    private ObtainedCertificationsAdapter adapter;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obtained_certifications);

        // Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        uid = user.getUid();

        ImageView ivBack     = findViewById(R.id.ivBack);
        rvObtainedCerts      = findViewById(R.id.rvObtainedCerts);
        progressLoading      = findViewById(R.id.progressLoading);
        layoutEmpty          = findViewById(R.id.layoutEmpty);
        btnStartLearning     = findViewById(R.id.btnStartLearning);

        ivBack.setOnClickListener(v -> finish());

        btnStartLearning.setOnClickListener(v ->
                startActivity(new Intent(this, CareerProfileActivity.class)));

        certList = new ArrayList<>();
        adapter = new ObtainedCertificationsAdapter(this, certList);
        rvObtainedCerts.setLayoutManager(new LinearLayoutManager(this));
        rvObtainedCerts.setAdapter(adapter);

        fetchObtainedCertifications();
    }

    private void fetchObtainedCertifications() {
        progressLoading.setVisibility(View.VISIBLE);
        rvObtainedCerts.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        db.collection("users").document(uid)
                .collection("certification_progress")
                .whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    certList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        if (doc.get("completedAt") != null) {
                            Map<String, Object> certData = new HashMap<>();
                            certData.put("certificationName",
                                    doc.getString("certificationName"));
                            certData.put("provider", doc.getString("provider"));
                            certData.put("completedAt", doc.get("completedAt"));
                            certData.put("totalModules", doc.get("totalModules"));

                            Object scoreObj = doc.get("totalScore");
                            int score = scoreObj instanceof Number
                                    ? ((Number) scoreObj).intValue() : 0;

                            if (score == 0) {

                                Map<String, Object> quizScores =
                                        (Map<String, Object>) doc.get("quizScores");
                                if (quizScores != null && !quizScores.isEmpty()) {
                                    int sum = 0;
                                    for (Object v : quizScores.values()) {
                                        if (v instanceof Number) sum += ((Number) v).intValue();
                                    }
                                    score = sum / quizScores.size();
                                }
                            }

                            certData.put("totalScore", score);
                            certList.add(certData);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    progressLoading.setVisibility(View.GONE);

                    if (certList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvObtainedCerts.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvObtainedCerts.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch obtained certifications", e);
                    progressLoading.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this,
                            "Could not load certifications.", Toast.LENGTH_SHORT).show();
                });
    }
}
