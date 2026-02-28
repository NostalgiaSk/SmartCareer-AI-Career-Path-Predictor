package com.hanin.smartcareer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.firebase.AuthRepository;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private TextView tvCreateAccount;

    private ProgressBar progressBar;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository();

        initViews();
        setupListeners();
        setupCreateAccountText();

        // If user is already signed in, check their role and navigate
        if (authRepository.getCurrentUser() != null) {
            navigateByRole();
        }
    }


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
        btnLogin.setOnClickListener(v -> attemptLogin());
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

        int start = fullText.indexOf(clickablePart);
        int end   = start + clickablePart.length();

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

    private void attemptLogin() {
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

    private void loginWithFirebase(String email, String password) {
        // Show progress spinner and disable button to prevent double-taps
        setLoading(true);

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        R.string.toast_login_success, Toast.LENGTH_SHORT).show();

                // Check user role and navigate to the appropriate dashboard
                navigateByRole();
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


    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
    }

    // ── Role-based navigation ───────────────────────────────────────

    private void navigateByRole() {
        // Get the currently signed-in Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Safety check: if user is null, stay on login
        if (user == null) {
            Log.w("LoginActivity", "navigateByRole: user is null");
            return;
        }

        String uid = user.getUid();

        // Query Firestore for the user's role
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Intent intent;

                    if (snapshot.exists()
                            && "admin".equals(snapshot.getString("role"))) {
                        // ✅ Admin user → open AdminDashboardActivity
                        intent = new Intent(LoginActivity.this,
                                AdminDashboardActivity.class);
                    } else {
                        // Regular student → open HomeActivity
                        intent = new Intent(LoginActivity.this,
                                HomeActivity.class);
                    }

                    // Clear the back stack so the user can't go back to login
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Firestore read failed — default to HomeActivity
                    Log.e("LoginActivity",
                            "navigateByRole: Firestore read failed", e);
                    Intent intent = new Intent(LoginActivity.this,
                            HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}
