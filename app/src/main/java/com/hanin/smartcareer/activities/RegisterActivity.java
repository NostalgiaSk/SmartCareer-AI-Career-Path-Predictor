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

public class RegisterActivity extends AppCompatActivity {

    // ── View references ─────────────────────────────────────────────

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialise the Firebase repository
        authRepository = new AuthRepository();

        initViews();
        setupListeners();
        setupLoginLinkText();
    }

    // ── View initialisation ─────────────────────────────────────────

    private void initViews() {
        tilFullName        = findViewById(R.id.tilFullName);
        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etFullName         = findViewById(R.id.etFullName);
        etEmail            = findViewById(R.id.etEmail);
        etPassword         = findViewById(R.id.etPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);
        btnRegister        = findViewById(R.id.btnRegister);
        tvLoginLink        = findViewById(R.id.tvLoginLink);
        progressBar        = findViewById(R.id.progressBar);
    }

    // ── Listeners ───────────────────────────────────────────────────

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());
    }


    private void setupLoginLinkText() {
        String fullText      = getString(R.string.has_account) + getString(R.string.link_login);
        String clickablePart = getString(R.string.link_login);

        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf(clickablePart);
        int end   = start + clickablePart.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navigate back to LoginActivity
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish(); 
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

        tvLoginLink.setText(spannable);
        tvLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // ── Validation ──────────────────────────────────────────────────

    private void attemptRegistration() {
        // Read current input values (trim whitespace)
        String fullName        = etFullName.getText()        != null ? etFullName.getText().toString().trim()        : "";
        String email           = etEmail.getText()           != null ? etEmail.getText().toString().trim()           : "";
        String password        = etPassword.getText()        != null ? etPassword.getText().toString().trim()        : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Reset any previous error messages
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean isValid = true;

        // ── Full-name validation ────────────────────────────────────
        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_name_empty));
            isValid = false;
        }

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

        // ── Confirm-password validation ─────────────────────────────
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.error_confirm_password_empty));
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_mismatch));
            isValid = false;
        }

        // ── Proceed if all checks pass ──────────────────────────────
        if (isValid) {
            registerWithFirebase(fullName, email, password);
        }
    }

    // ── Firebase Registration ───────────────────────────────────────

    private void registerWithFirebase(String fullName, String email, String password) {
        // Show progress spinner and disable button to prevent double-taps
        setLoading(true);

        authRepository.register(fullName, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        R.string.toast_register_success, Toast.LENGTH_SHORT).show();

                // Navigate to the Home Dashboard and clear back stack
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── UI helpers ──────────────────────────────────────────────────

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
    }
}
