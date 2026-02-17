package com.hanin.smartcareer;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class LoginActivity extends AppCompatActivity {

    // ── View references ─────────────────────────────────────────────

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private TextView tvCreateAccount;

    // ── Lifecycle ───────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        initViews();
        setupListeners();
        setupCreateAccountText();
    }

    // ── View initialisation ─────────────────────────────────────────
    private void initViews() {
        tilEmail        = findViewById(R.id.tilEmail);
        tilPassword     = findViewById(R.id.tilPassword);
        etEmail         = findViewById(R.id.etEmail);
        etPassword      = findViewById(R.id.etPassword);
        btnLogin        = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount  = findViewById(R.id.tvCreateAccount);
    }

    // ── Listeners ───────────────────────────────────────────────────
    private void setupListeners() {

        // ── Login button ────────────────────────────────────────────
        btnLogin.setOnClickListener(v -> attemptLogin());

        // ── Forgot Password ─────────────────────────────────────────
        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this,
                        R.string.toast_forgot_password,
                        Toast.LENGTH_SHORT).show()
        );
    }
    private void setupCreateAccountText() {
        String fullText      = getString(R.string.no_account) + getString(R.string.create_account);
        String clickablePart = getString(R.string.create_account);

        SpannableString spannable = new SpannableString(fullText);

        // Find the position of "Create Account" within the full string
        int start = fullText.indexOf(clickablePart);
        int end   = start + clickablePart.length();

        // Define what happens when the user taps the span
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navigate to the registration screen
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                // Style the clickable portion: brand colour, bold, no underline
                ds.setColor(getResources().getColor(R.color.sc_text_link, getTheme()));
                ds.setFakeBoldText(true);
                ds.setUnderlineText(false);
            }
        };

        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvCreateAccount.setText(spannable);
        tvCreateAccount.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // ── Validation ──────────────────────────────────────────────────

    /**
     * Validates the form and, if everything is correct, proceeds to
     * authenticate the user.
     */
    private void attemptLogin() {
        // Read current input values (trim whitespace)
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Reset any previous error messages
        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;

        // ── Email validation ────────────────────────────────────────
        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_email_empty));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            isValid = false;
        }

        // ── Password validation ─────────────────────────────────────
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_empty));
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            isValid = false;
        }

        // ── Proceed if all checks pass ──────────────────────────────
        if (isValid) {
            Toast.makeText(this, R.string.toast_login_success, Toast.LENGTH_SHORT).show();

            // Navigate to the Career Profile screen
            startActivity(new Intent(LoginActivity.this, CareerProfileActivity.class));
            finish();

            // Hand off to Firebase (placeholder)
            loginWithFirebase(email, password);
        }
    }

    // ── Firebase Authentication (placeholder) ───────────────────────

    /**
     * Placeholder method for Firebase Authentication.
     * <p>
     * To integrate Firebase:
     * <ol>
     *   <li>Add the Firebase SDK to your project (google-services plugin + BOM).</li>
     *   <li>Initialise {@code FirebaseAuth} in {@link #onCreate}.</li>
     *   <li>Call {@code firebaseAuth.signInWithEmailAndPassword(email, password)}
     *       and handle the {@code Task} result here.</li>
     *   <li>On success → navigate to the main dashboard.</li>
     *   <li>On failure → display the error message to the user.</li>
     * </ol>
     *
     * @param email    The validated email address entered by the user.
     * @param password The validated password entered by the user.
     */
    private void loginWithFirebase(String email, String password) {
        // TODO: Implement Firebase Authentication sign-in
        //
        // Example (uncomment when Firebase is configured):
        //
        // FirebaseAuth auth = FirebaseAuth.getInstance();
        // auth.signInWithEmailAndPassword(email, password)
        //     .addOnCompleteListener(this, task -> {
        //         if (task.isSuccessful()) {
        //             // Sign-in success – navigate to dashboard
        //             startActivity(new Intent(LoginActivity.this, MainActivity.class));
        //             finish();
        //         } else {
        //             // Sign-in failed – show error
        //             String errorMsg = task.getException() != null
        //                     ? task.getException().getMessage()
        //                     : "Authentication failed";
        //             Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        //         }
        //     });
    }
}
