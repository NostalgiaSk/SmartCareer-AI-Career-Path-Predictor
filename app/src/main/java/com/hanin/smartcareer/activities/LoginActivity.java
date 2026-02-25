package com.hanin.smartcareer.activities;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.firebase.AuthRepository;


/**
 * Login screen – the first screen the user sees.
 * <p>
 * Uses {@link AuthRepository} to authenticate via Firebase Auth and
 * automatically fetch/create the Firestore user document before
 * navigating to {@link HomeActivity}.
 */
public class LoginActivity extends AppCompatActivity {

    // ── View references ─────────────────────────────────────────────

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private TextView tvCreateAccount;

    /** Spinner shown while the Firebase request is in progress. */
    private ProgressBar progressBar;

    // ── Firebase ────────────────────────────────────────────────────

    /** Repository that encapsulates all Auth + Firestore logic. */
    private AuthRepository authRepository;

    // ── Lifecycle ───────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Initialise the Firebase repository
        authRepository = new AuthRepository();

        initViews();
        setupListeners();
        setupCreateAccountText();

        // If the user is already signed in, skip login and go to Home
        if (authRepository.getCurrentUser() != null) {
            navigateToHome();
        }
    }

    // ── View initialisation ─────────────────────────────────────────

    private void initViews() {
        tilEmail         = findViewById(R.id.tilEmail);
        tilPassword      = findViewById(R.id.tilPassword);
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvCreateAccount  = findViewById(R.id.tvCreateAccount);
        progressBar      = findViewById(R.id.progressBar);
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

    /**
     * Builds the "Don't have an account? Create Account" spannable text
     * and makes the "Create Account" portion clickable.
     */
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
     * authenticate the user via Firebase.
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
            loginWithFirebase(email, password);
        }
    }

    // ── Firebase Authentication ─────────────────────────────────────

    /**
     * Signs in the user with Firebase Auth via the {@link AuthRepository}.
     * <p>
     * On success the repository also verifies that the user's Firestore
     * document exists (creating it if missing) before the activity
     * navigates to {@link HomeActivity}.
     *
     * @param email    Validated email address.
     * @param password Validated password.
     */
    private void loginWithFirebase(String email, String password) {
        // Show progress spinner and disable button to prevent double-taps
        setLoading(true);

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        R.string.toast_login_success, Toast.LENGTH_SHORT).show();

                // Navigate to the Home Dashboard
                navigateToHome();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── UI helpers ──────────────────────────────────────────────────

    /**
     * Toggles the loading state: shows/hides the progress bar and
     * enables/disables the login button.
     *
     * @param isLoading {@code true} to show spinner and disable button.
     */
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
    }

    /**
     * Navigates to {@link HomeActivity} and clears the back stack so
     * the user cannot press Back to return to the login screen.
     */
    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
