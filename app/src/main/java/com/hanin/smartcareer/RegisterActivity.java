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

    // ── Lifecycle ───────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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
    }

    // ── Listeners ───────────────────────────────────────────────────

    private void setupListeners() {
        // ── Register button ─────────────────────────────────────────
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
                finish(); // Remove RegisterActivity from back stack
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

    /**
     * Validates all four fields and, if everything passes, proceeds to
     * create the user account.
     */
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
            Toast.makeText(this, R.string.toast_register_success, Toast.LENGTH_SHORT).show();

            // Hand off to Firebase (placeholder)
            registerWithFirebase(fullName, email, password);
        }
    }

    // ── Firebase Authentication (placeholder) ───────────────────────

    /**
     * Placeholder method for Firebase Authentication registration.
     * <p>
     * To integrate Firebase:
     * <ol>
     *   <li>Add the Firebase SDK to your project (google-services plugin + BOM).</li>
     *   <li>Initialise {@code FirebaseAuth} in {@link #onCreate}.</li>
     *   <li>Call {@code firebaseAuth.createUserWithEmailAndPassword(email, password)}
     *       and handle the {@code Task} result here.</li>
     *   <li>Optionally store the {@code fullName} in Firestore or Realtime Database
     *       under the newly created user's UID.</li>
     *   <li>On success → navigate to the main dashboard.</li>
     *   <li>On failure → display the error message to the user.</li>
     * </ol>
     *
     * @param fullName The user's full name.
     * @param email    The validated email address entered by the user.
     * @param password The validated password entered by the user.
     */
    private void registerWithFirebase(String fullName, String email, String password) {
        // TODO: Implement Firebase Authentication registration
        //
        // Example (uncomment when Firebase is configured):
        //
        // FirebaseAuth auth = FirebaseAuth.getInstance();
        // auth.createUserWithEmailAndPassword(email, password)
        //     .addOnCompleteListener(this, task -> {
        //         if (task.isSuccessful()) {
        //             // Registration success
        //             FirebaseUser user = auth.getCurrentUser();
        //
        //             // Optionally update the user's display name
        //             // UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
        //             //         .setDisplayName(fullName)
        //             //         .build();
        //             // user.updateProfile(profileUpdate);
        //
        //             // Navigate to the main dashboard
        //             startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        //             finish();
        //         } else {
        //             // Registration failed – show error
        //             String errorMsg = task.getException() != null
        //                     ? task.getException().getMessage()
        //                     : "Registration failed";
        //             Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        //         }
        //     });
    }
}
