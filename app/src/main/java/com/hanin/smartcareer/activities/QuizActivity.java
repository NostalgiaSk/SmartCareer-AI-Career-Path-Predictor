package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Module;
import com.hanin.smartcareer.models.QuizQuestion;

import java.util.ArrayList;

/**
 * Quiz screen for module assessment.
 *
 * <p>Displays questions one at a time with 4 selectable options.
 * After the last question, a result overlay shows the score and
 * whether the student passed (≥ 70%). Results are returned to
 * the calling activity via {@code setResult()}.</p>
 *
 * <p>Pass threshold: 70% (at least 2 out of 3 correct).</p>
 */
public class QuizActivity extends AppCompatActivity {

    // ── Constants ───────────────────────────────────────────────────
    private static final int PASS_THRESHOLD = 70; // 70% required to pass

    // ── Views ───────────────────────────────────────────────────────
    private TextView tvQuestionCounter, tvQuestion;
    private ProgressBar progressQuiz;
    private TextView[] optionViews;        // The 4 option TextViews
    private Button btnNext;
    private FrameLayout resultOverlay;
    private TextView tvResultTitle, tvResultScore;
    private Button btnResultAction;

    // ── Data ────────────────────────────────────────────────────────
    private Module module;
    private ArrayList<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private int selectedOptionIndex  = -1;  // -1 = nothing selected
    private int correctAnswers       = 0;

    // ── Lifecycle ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // 1. Retrieve module data
        module = (Module) getIntent().getSerializableExtra("module_data");
        if (module == null || module.getQuizQuestions() == null
                || module.getQuizQuestions().isEmpty()) {
            Toast.makeText(this, "No quiz data available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        questions = module.getQuizQuestions();

        // 2. Bind all views
        initViews();

        // 3. Set up click listeners
        setupListeners();

        // 4. Display the first question
        showQuestion(0);
    }

    // ── View initialisation ─────────────────────────────────────────

    private void initViews() {
        ImageView ivBack   = findViewById(R.id.ivBack);
        tvQuestionCounter  = findViewById(R.id.tvQuestionCounter);
        progressQuiz       = findViewById(R.id.progressQuiz);
        tvQuestion         = findViewById(R.id.tvQuestion);
        btnNext            = findViewById(R.id.btnNext);
        resultOverlay      = findViewById(R.id.resultOverlay);
        tvResultTitle      = findViewById(R.id.tvResultTitle);
        tvResultScore      = findViewById(R.id.tvResultScore);
        btnResultAction    = findViewById(R.id.btnResultAction);

        // The 4 option TextViews
        optionViews = new TextView[4];
        optionViews[0] = findViewById(R.id.tvOption0);
        optionViews[1] = findViewById(R.id.tvOption1);
        optionViews[2] = findViewById(R.id.tvOption2);
        optionViews[3] = findViewById(R.id.tvOption3);

        // Back arrow
        ivBack.setOnClickListener(v -> finish());
    }

    // ── Click listeners ─────────────────────────────────────────────

    private void setupListeners() {
        // Option selection — each option is a clickable TextView
        for (int i = 0; i < 4; i++) {
            final int index = i;
            optionViews[i].setOnClickListener(v -> selectOption(index));
        }

        // Next / Submit button
        btnNext.setOnClickListener(v -> {
            if (selectedOptionIndex == -1) {
                // No option selected
                Toast.makeText(this, getString(R.string.quiz_select_answer),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if answer is correct
            QuizQuestion currentQ = questions.get(currentQuestionIndex);
            if (selectedOptionIndex == currentQ.getCorrectIndex()) {
                correctAnswers++;
            }

            // Show correct/wrong feedback briefly, then advance
            showAnswerFeedback(currentQ.getCorrectIndex());

            // Use a short delay so the user sees the feedback colours
            btnNext.postDelayed(() -> {
                if (currentQuestionIndex < questions.size() - 1) {
                    // Go to next question
                    currentQuestionIndex++;
                    showQuestion(currentQuestionIndex);
                } else {
                    // All questions answered — show result
                    showResult();
                }
            }, 800);
        });
    }

    // ── Display a question ──────────────────────────────────────────

    /**
     * Populates the UI with the question at the given index.
     *
     * @param index 0-based index into the questions list.
     */
    private void showQuestion(int index) {
        QuizQuestion q = questions.get(index);

        // Reset selection state
        selectedOptionIndex = -1;

        // Update counter and progress
        tvQuestionCounter.setText(getString(R.string.quiz_question_counter,
                index + 1, questions.size()));
        int progressPercent = (int) (((index) / (float) questions.size()) * 100);
        progressQuiz.setProgress(progressPercent);

        // Set question text
        tvQuestion.setText(q.getQuestion());

        // Set option texts and reset backgrounds
        String[] prefixes = {"A. ", "B. ", "C. ", "D. "};
        for (int i = 0; i < 4; i++) {
            if (i < q.getOptions().size()) {
                optionViews[i].setText(prefixes[i] + q.getOptions().get(i));
                optionViews[i].setVisibility(View.VISIBLE);
            } else {
                optionViews[i].setVisibility(View.GONE);
            }
            optionViews[i].setBackgroundResource(R.drawable.bg_quiz_option);
            optionViews[i].setSelected(false);
            optionViews[i].setClickable(true);
        }

        // Update button text: "Next" or "Submit" on last question
        if (index == questions.size() - 1) {
            btnNext.setText(R.string.quiz_submit);
        } else {
            btnNext.setText(R.string.quiz_next);
        }
    }

    // ── Option selection ────────────────────────────────────────────

    /**
     * Highlights the selected option and deselects all others.
     *
     * @param index The 0-based index of the selected option.
     */
    private void selectOption(int index) {
        selectedOptionIndex = index;

        // Update visual state for all options
        for (int i = 0; i < 4; i++) {
            optionViews[i].setSelected(i == index);
            optionViews[i].setBackgroundResource(R.drawable.bg_quiz_option);
        }
    }

    // ── Answer feedback ─────────────────────────────────────────────

    /**
     * Shows green/red backgrounds on the correct and selected options.
     * Disables further clicks while feedback is shown.
     *
     * @param correctIndex The 0-based index of the correct answer.
     */
    private void showAnswerFeedback(int correctIndex) {
        // Disable all options during feedback
        for (int i = 0; i < 4; i++) {
            optionViews[i].setClickable(false);
        }

        // Highlight correct answer in green
        optionViews[correctIndex].setBackgroundResource(R.drawable.bg_quiz_option_correct);

        // If wrong, highlight the selected one in red
        if (selectedOptionIndex != correctIndex) {
            optionViews[selectedOptionIndex].setBackgroundResource(
                    R.drawable.bg_quiz_option_wrong);
        }

        // Disable the Next/Submit button during feedback
        btnNext.setEnabled(false);
        btnNext.postDelayed(() -> btnNext.setEnabled(true), 800);
    }

    // ── Show quiz result ────────────────────────────────────────────

    // Request code for QuizResultActivity
    private static final int RESULT_REQUEST_CODE = 3001;

    /**
     * Calculates the score and launches {@link QuizResultActivity}
     * instead of showing an in-line overlay.
     */
    private void showResult() {
        int totalQuestions = questions.size();
        int scorePercent = (int) ((correctAnswers / (float) totalQuestions) * 100);
        boolean passed = scorePercent >= PASS_THRESHOLD;

        // Update progress bar to 100%
        progressQuiz.setProgress(100);

        // Get certification name from the intent chain (passed from CertificationDetailActivity)
        String certName = getIntent().getStringExtra("certification_name");

        // Launch QuizResultActivity
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra(QuizResultActivity.EXTRA_PASSED, passed);
        intent.putExtra(QuizResultActivity.EXTRA_SCORE, scorePercent);
        intent.putExtra(QuizResultActivity.EXTRA_CORRECT, correctAnswers);
        intent.putExtra(QuizResultActivity.EXTRA_TOTAL, totalQuestions);
        intent.putExtra(QuizResultActivity.EXTRA_MODULE_ID, module.getModuleId());
        intent.putExtra(QuizResultActivity.EXTRA_CERT_NAME, certName);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    // ── Handle QuizResultActivity response ──────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                // PASSED → forward result back to ModuleActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("quiz_passed", data.getBooleanExtra(
                        QuizResultActivity.EXTRA_PASSED, true));
                resultIntent.putExtra("quiz_score", data.getIntExtra(
                        QuizResultActivity.EXTRA_SCORE, 0));
                resultIntent.putExtra("module_id", data.getStringExtra(
                        QuizResultActivity.EXTRA_MODULE_ID));
                setResult(RESULT_OK, resultIntent);
                finish();

            } else if (resultCode == QuizResultActivity.RESULT_RETRY) {
                // RETRY → reset quiz state and start over
                currentQuestionIndex = 0;
                correctAnswers = 0;
                selectedOptionIndex = -1;
                showQuestion(0);

            } else {
                // QUIT → return to CertificationDetailActivity (cancel)
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }
}

