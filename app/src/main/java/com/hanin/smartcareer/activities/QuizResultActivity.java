package com.hanin.smartcareer.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays the quiz result in a dedicated full-screen activity.
 *
 * <h3>Two states</h3>
 * <ul>
 *   <li><b>PASSED (score ≥ 70%)</b> — green theme, "✅" icon, "Continue" button.
 *       Returns {@code RESULT_OK} with quiz_passed=true.</li>
 *   <li><b>FAILED (score &lt; 70%)</b> — red theme, "❌" icon, "Retry" + "Quit" buttons.
 *       Saves failed attempt to Firestore. Retry returns {@code RESULT_FIRST_USER}
 *       (QuizActivity resets). Quit returns {@code RESULT_CANCELED}.</li>
 * </ul>
 *
 * <p>Every quiz attempt (pass or fail) is saved to Firestore under:
 * {@code users/{uid}/certification_progress/{certId}/moduleAttempts/{moduleId}}</p>
 */
public class QuizResultActivity extends AppCompatActivity {

    private static final String TAG = "QuizResultActivity";

    // ── Intent extra keys ────────────────────────────────────────────
    public static final String EXTRA_PASSED        = "quiz_passed";
    public static final String EXTRA_SCORE         = "quiz_score";
    public static final String EXTRA_CORRECT       = "quiz_correct";
    public static final String EXTRA_TOTAL         = "quiz_total";
    public static final String EXTRA_MODULE_ID     = "module_id";
    public static final String EXTRA_CERT_NAME     = "certification_name";

    // Result code for "retry" — distinct from RESULT_OK and RESULT_CANCELED
    public static final int RESULT_RETRY = RESULT_FIRST_USER;

    // ── Views ────────────────────────────────────────────────────────
    private TextView tvResultIcon, tvResultTitle, tvScorePercent;
    private TextView tvCorrectCount, tvMinRequired;
    private Button btnPrimaryAction, btnSecondaryAction;

    // ── Data ─────────────────────────────────────────────────────────
    private boolean passed;
    private int score, correct, total;
    private String moduleId, certName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // 1. Extract intent data
        passed   = getIntent().getBooleanExtra(EXTRA_PASSED, false);
        score    = getIntent().getIntExtra(EXTRA_SCORE, 0);
        correct  = getIntent().getIntExtra(EXTRA_CORRECT, 0);
        total    = getIntent().getIntExtra(EXTRA_TOTAL, 0);
        moduleId = getIntent().getStringExtra(EXTRA_MODULE_ID);
        certName = getIntent().getStringExtra(EXTRA_CERT_NAME);

        // 2. Bind views
        tvResultIcon       = findViewById(R.id.tvResultIcon);
        tvResultTitle      = findViewById(R.id.tvResultTitle);
        tvScorePercent     = findViewById(R.id.tvScorePercent);
        tvCorrectCount     = findViewById(R.id.tvCorrectCount);
        tvMinRequired      = findViewById(R.id.tvMinRequired);
        btnPrimaryAction   = findViewById(R.id.btnPrimaryAction);
        btnSecondaryAction = findViewById(R.id.btnSecondaryAction);

        // 3. Configure UI based on pass/fail state
        if (passed) {
            setupPassState();
        } else {
            setupFailState();
        }

        // 4. Animate the result icon
        animateIcon();

        // 5. Save attempt to Firestore
        saveAttemptToFirestore();
    }

    // ══════════════════════════════════════════════════════════════════
    // PASS STATE
    // ══════════════════════════════════════════════════════════════════

    private void setupPassState() {
        // Green theme
        tvResultIcon.setText("✅");
        tvResultTitle.setText(getString(R.string.quiz_result_pass));
        tvResultTitle.setTextColor(ContextCompat.getColor(this, R.color.sc_success));

        // Score display
        tvScorePercent.setText(score + "%");
        tvScorePercent.setTextColor(ContextCompat.getColor(this, R.color.sc_success));
        tvCorrectCount.setText(getString(R.string.quiz_correct_count, correct, total));
        tvMinRequired.setVisibility(View.GONE);

        // Primary button: Continue to Next Module
        btnPrimaryAction.setText(getString(R.string.quiz_continue_next));
        btnPrimaryAction.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_PASSED, true);
            result.putExtra(EXTRA_SCORE, score);
            result.putExtra(EXTRA_MODULE_ID, moduleId);
            setResult(RESULT_OK, result);
            finish();
        });

        // No secondary button for pass state
        btnSecondaryAction.setVisibility(View.GONE);
    }

    // ══════════════════════════════════════════════════════════════════
    // FAIL STATE
    // ══════════════════════════════════════════════════════════════════

    private void setupFailState() {
        // Red theme
        tvResultIcon.setText("❌");
        tvResultTitle.setText(getString(R.string.quiz_result_fail));
        tvResultTitle.setTextColor(ContextCompat.getColor(this, R.color.sc_quiz_wrong));

        // Score display
        tvScorePercent.setText(score + "%");
        tvScorePercent.setTextColor(ContextCompat.getColor(this, R.color.sc_quiz_wrong));
        tvCorrectCount.setText(getString(R.string.quiz_correct_count, correct, total));
        tvMinRequired.setVisibility(View.VISIBLE);

        // Primary button: Retry Quiz
        btnPrimaryAction.setText(getString(R.string.quiz_retry));
        btnPrimaryAction.setOnClickListener(v -> {
            setResult(RESULT_RETRY);
            finish();
        });

        // Secondary button: Quit (return to CertificationDetailActivity)
        btnSecondaryAction.setVisibility(View.VISIBLE);
        btnSecondaryAction.setText(getString(R.string.quiz_quit));
        btnSecondaryAction.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    // ══════════════════════════════════════════════════════════════════
    // ICON ANIMATION
    // ══════════════════════════════════════════════════════════════════

    /**
     * Animates the result icon with a scale-up + overshoot effect
     * to draw attention to the pass/fail status.
     */
    private void animateIcon() {
        tvResultIcon.setScaleX(0f);
        tvResultIcon.setScaleY(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvResultIcon, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvResultIcon, "scaleY", 0f, 1f);

        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setInterpolator(new OvershootInterpolator(2f));
        scaleY.setInterpolator(new OvershootInterpolator(2f));
        scaleX.setStartDelay(200);
        scaleY.setStartDelay(200);

        scaleX.start();
        scaleY.start();
    }

    // ══════════════════════════════════════════════════════════════════
    // FIRESTORE: SAVE ATTEMPT
    // ══════════════════════════════════════════════════════════════════

    /**
     * Saves every quiz attempt (pass or fail) to Firestore under:
     * {@code users/{uid}/certification_progress/{certId}/moduleAttempts/{moduleId}}
     *
     * <p>Each attempt record includes score, passed boolean, and timestamp.</p>
     */
    private void saveAttemptToFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null || certName == null || moduleId == null) return;

        // Sanitize cert name for document ID
        String certDocId = certName.replaceAll("[^a-zA-Z0-9 ]", "")
                .trim().replaceAll("\\s+", "_");

        Map<String, Object> attempt = new HashMap<>();
        attempt.put("score", score);
        attempt.put("passed", passed);
        attempt.put("correctAnswers", correct);
        attempt.put("totalQuestions", total);
        attempt.put("attemptedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("certification_progress").document(certDocId)
                .collection("moduleAttempts").document(moduleId)
                .set(attempt)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Attempt saved: " + moduleId + " score=" + score))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to save attempt", e));
    }

    // Prevent back button from skipping the result
    @Override
    public void onBackPressed() {
        // On pass, return result. On fail, treat as quit.
        if (passed) {
            Intent result = new Intent();
            result.putExtra(EXTRA_PASSED, true);
            result.putExtra(EXTRA_SCORE, score);
            result.putExtra(EXTRA_MODULE_ID, moduleId);
            setResult(RESULT_OK, result);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.onBackPressed();
    }
}
