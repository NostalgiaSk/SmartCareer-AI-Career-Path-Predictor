package com.hanin.smartcareer.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.CertificationAdapter;
import com.hanin.smartcareer.adapters.SkillGapAdapter;
import com.hanin.smartcareer.models.Career;
import com.hanin.smartcareer.models.Certification;
import com.hanin.smartcareer.models.SkillGap;

import java.util.ArrayList;
import java.util.List;


public class SkillGapActivity extends AppCompatActivity {

    private TextView tvCareerName;
    private TextView tvMatchScore;
    private TextView tvDescription;
    private RecyclerView rvSkillGaps;
    private RecyclerView rvCertifications;
    private SkillGapAdapter skillGapAdapter;
    private CertificationAdapter certificationAdapter;
    private List<SkillGap> skillGapList;
    private List<Certification> certificationList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_gap);

        // 1. Bind views
        initViews();

        // 2. Extract the Career object sent by CareerSuggestionsActivity
        Career career = (Career) getIntent().getSerializableExtra("career_data");

        // 3. Populate the header and lists
        if (career != null) {
            bindCareerHeader(career);
            buildSkillGapList(career.getSkillGaps());
            buildCertificationList(career.getCertifications());
        } else {
            // Fallback: use dummy data so the screen is still testable
            // without a real Intent (e.g., when launched from Android Studio).
            bindDummyHeader();
            loadDummySkillGaps();
            loadDummyCertifications();
        }

        // 4. Wire up both RecyclerViews
        setupSkillGapsRecyclerView();
        setupCertificationsRecyclerView();

        // 5. Firebase placeholder (no-op until backend is ready)
        fetchCareerDetailsFromFirestore("placeholder_career_id");
    }

    // ── View initialisation ─────────────────────────────────────────

    private void initViews() {
        tvCareerName      = findViewById(R.id.tvCareerName);
        tvMatchScore      = findViewById(R.id.tvMatchScore);
        tvDescription     = findViewById(R.id.tvDescription);
        rvSkillGaps       = findViewById(R.id.rvSkillGaps);
        rvCertifications  = findViewById(R.id.rvCertifications);
    }

    // ── Header binding ──────────────────────────────────────────────
    private void bindCareerHeader(Career career) {
        tvCareerName.setText(career.getCareerName());
        tvDescription.setText(career.getDescription());

        String scoreText = career.getMatchScore() + " % Match";
        tvMatchScore.setText(scoreText);
    }

    private void bindDummyHeader() {
        tvCareerName.setText("AI Engineer");
        tvDescription.setText("Design and build intelligent systems powered by machine learning.");
        tvMatchScore.setText("92 % Match");
    }

    // ══════════════════════════════════════════════════════════════════
    // SECTION 1 — SKILL GAPS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Converts the raw list of skill-name strings received from the
     * {@link Career} object into richer {@link SkillGap} models,
     * each paired with a contextual explanation.
     *
     * @param rawSkills The {@code Career.getSkillGaps()} list.
     */
    private void buildSkillGapList(ArrayList<String> rawSkills) {
        skillGapList = new ArrayList<>();

        // Map each raw string to a SkillGap with a contextual explanation.
        // In production, explanations would come from Firestore or an AI API.
        for (String skill : rawSkills) {
            String explanation = getExplanationForSkill(skill);
            skillGapList.add(new SkillGap(skill, explanation));
        }
    }

    /**
     * Returns a placeholder description for well-known skills.
     * In production, this would be replaced by data from Firestore
     * or an AI-generated response.
     */
    private String getExplanationForSkill(String skillName) {
        switch (skillName) {
            case "TensorFlow":
                return "Core framework for building and deploying ML models at scale.";
            case "PyTorch":
                return "Flexible deep-learning library favoured in research and production.";
            case "MLOps":
                return "Practices for reliable ML model deployment, monitoring, and retraining.";
            case "SQL":
                return "Essential language for querying and managing relational databases.";
            case "Power BI":
                return "Microsoft's platform for interactive data visualisation dashboards.";
            case "Pandas":
                return "Python library for fast, expressive data manipulation and analysis.";
            case "Agile / Scrum":
                return "Iterative development framework promoting rapid delivery cycles.";
            case "User Research":
                return "Techniques for understanding user needs through interviews and testing.";
            case "A/B Testing":
                return "Controlled experiments to compare feature variants with real users.";
            default:
                return "Important skill for career growth — explore online courses to get started.";
        }
    }

    /** Populates {@code skillGapList} with hard-coded data for testing. */
    private void loadDummySkillGaps() {
        skillGapList = new ArrayList<>();
        skillGapList.add(new SkillGap("TensorFlow",
                "Core framework for building and deploying ML models at scale."));
        skillGapList.add(new SkillGap("PyTorch",
                "Flexible deep-learning library favoured in research and production."));
        skillGapList.add(new SkillGap("MLOps",
                "Practices for reliable ML model deployment, monitoring, and retraining."));
    }

    /** Sets up the skill-gaps RecyclerView with a vertical layout manager. */
    private void setupSkillGapsRecyclerView() {
        skillGapAdapter = new SkillGapAdapter(skillGapList);
        rvSkillGaps.setLayoutManager(new LinearLayoutManager(this));
        rvSkillGaps.setAdapter(skillGapAdapter);
    }

    // ══════════════════════════════════════════════════════════════════
    // SECTION 2 — CERTIFICATIONS
    // ══════════════════════════════════════════════════════════════════

    /**
     * Converts the raw list of certification-name strings from the
     * {@link Career} object into richer {@link Certification} models,
     * each enriched with provider and difficulty metadata.
     *
     * @param rawCerts The {@code Career.getCertifications()} list.
     */
    private void buildCertificationList(ArrayList<String> rawCerts) {
        certificationList = new ArrayList<>();

        for (String certName : rawCerts) {
            String[] details = getDetailsForCertification(certName);
            certificationList.add(new Certification(certName, details[0], details[1]));
        }
    }

    /**
     * Returns provider and difficulty for well-known certifications.
     * In production, this metadata would come from Firestore.
     *
     * @return A two-element array: {@code [provider, difficulty]}.
     */
    private String[] getDetailsForCertification(String certName) {
        switch (certName) {
            case "Google ML Engineer":
                return new String[]{"Google Cloud", "Advanced"};
            case "AWS ML Specialty":
                return new String[]{"Amazon Web Services", "Advanced"};
            case "Google Data Analytics":
                return new String[]{"Google", "Beginner"};
            case "IBM Data Analyst":
                return new String[]{"IBM", "Intermediate"};
            case "Certified Scrum Product Owner":
                return new String[]{"Scrum Alliance", "Intermediate"};
            case "PMI-ACP":
                return new String[]{"PMI", "Advanced"};
            default:
                return new String[]{"Various", "Intermediate"};
        }
    }

    /** Populates {@code certificationList} with hard-coded data for testing. */
    private void loadDummyCertifications() {
        certificationList = new ArrayList<>();
        certificationList.add(new Certification(
                "Google ML Engineer", "Google Cloud", "Advanced"));
        certificationList.add(new Certification(
                "AWS ML Specialty", "Amazon Web Services", "Advanced"));
    }

    /** Sets up the certifications RecyclerView with a vertical layout manager. */
    private void setupCertificationsRecyclerView() {
        certificationAdapter = new CertificationAdapter(certificationList);
        rvCertifications.setLayoutManager(new LinearLayoutManager(this));
        rvCertifications.setAdapter(certificationAdapter);
    }

    // ══════════════════════════════════════════════════════════════════
    // FIREBASE INTEGRATION (placeholder)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Placeholder for fetching detailed career data from Cloud Firestore.
     *
     * <h3>Integration steps</h3>
     * <ol>
     *   <li>Add the Firebase Firestore SDK to {@code build.gradle}:
     *       <pre>implementation 'com.google.firebase:firebase-firestore'</pre></li>
     *
     *   <li>Obtain the career document ID — either received via Intent or
     *       derived from the career name.</li>
     *
     *   <li>Query the career:
     *       <pre>
     *       FirebaseFirestore db = FirebaseFirestore.getInstance();
     *       db.collection("careers")
     *         .document(careerId)
     *         .get()
     *         .addOnSuccessListener(doc -&gt; {
     *             // 1. Parse skill-gap sub-collection → update skillGapList
     *             // 2. Parse certification sub-collection → update certificationList
     *             // 3. Notify both adapters:
     *             //    skillGapAdapter.notifyDataSetChanged();
     *             //    certificationAdapter.notifyDataSetChanged();
     *         })
     *         .addOnFailureListener(e -&gt; {
     *             Toast.makeText(this, "Error: " + e.getMessage(),
     *                            Toast.LENGTH_LONG).show();
     *         });
     *       </pre></li>
     *
     *   <li>Optionally show a {@code ProgressBar} while the query is in flight
     *       and hide the RecyclerViews until data arrives.</li>
     * </ol>
     *
     * @param careerId Document ID of the career in the "careers" Firestore collection.
     */
    private void fetchCareerDetailsFromFirestore(String careerId) {
        // TODO: Replace the dummy / Intent data with a real Firestore query.
        //       See Javadoc above for step-by-step instructions.
    }
}
