package com.hanin.smartcareer.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private static final String TAG = "AuthRepository";

    private static final String COLLECTION_USERS = "users";

    private final FirebaseAuth auth;

    private final FirebaseFirestore db;

    // ── Constructor ──────────────────────────────────────────────────

    public AuthRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db   = FirebaseFirestore.getInstance();
    }

    // ── Callback interfaces ─────────────────────────────────────────
    public interface AuthCallback {
        /** Called when the operation completes successfully. */
        void onSuccess();

        /** Called when the operation fails.
         *  @param errorMessage Human-readable error description. */
        void onFailure(String errorMessage);
    }

    // ── Public API ──────────────────────────────────────────────────
    public void register(@NonNull String fullName,
                         @NonNull String email,
                         @NonNull String password,
                         @NonNull AuthCallback callback) {

        // Step 1 – Create the Firebase Auth account
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        // Auth creation failed – report the error
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed. Please try again.";
                        Log.e(TAG, "register: Auth creation failed", task.getException());
                        callback.onFailure(error);
                        return;
                    }

                    // Step 2 – Auth account created; retrieve the new user
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Unexpected error: user is null after registration.");
                        return;
                    }

                    // Step 2a – Set the display name on the Auth profile
                    UserProfileChangeRequest profileUpdate =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                    firebaseUser.updateProfile(profileUpdate)
                            .addOnFailureListener(e ->
                                    Log.w(TAG, "register: display-name update failed", e));

                    // Step 3 – Build the Firestore document data
                    String uid = firebaseUser.getUid();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid",                  uid);
                    userData.put("email",                email);
                    userData.put("fullName",             fullName);
                    userData.put("role",                 "student");
                    userData.put("careerProfilesCount",  0);
                    userData.put("createdAt",            FieldValue.serverTimestamp());

                    // Step 4 – Write the document to Firestore (doc ID = UID)
                    db.collection(COLLECTION_USERS)
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "register: Firestore user document created");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                // Firestore write failed → delete the Auth user
                                // to avoid an orphan account
                                Log.e(TAG, "register: Firestore write failed, deleting auth user", e);
                                firebaseUser.delete()
                                        .addOnCompleteListener(deleteTask ->
                                                Log.d(TAG, "register: orphan auth user deleted"));
                                callback.onFailure("Account creation failed. Please try again.");
                            });
                });
    }


    public void login(@NonNull String email,
                      @NonNull String password,
                      @NonNull AuthCallback callback) {

        // Step 1 – Sign the user in with Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        // Auth sign-in failed
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Login failed. Please check your credentials.";
                        Log.e(TAG, "login: Auth sign-in failed", task.getException());
                        callback.onFailure(error);
                        return;
                    }

                    // Step 2 – Auth success; get the current user
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Unexpected error: user is null after login.");
                        return;
                    }

                    String uid = firebaseUser.getUid();

                    // Step 3 – Fetch the user document from Firestore
                    DocumentReference userRef =
                            db.collection(COLLECTION_USERS).document(uid);

                    userRef.get().addOnCompleteListener(docTask -> {
                        if (!docTask.isSuccessful()) {
                            Log.e(TAG, "login: Firestore read failed", docTask.getException());
                            // Even though Firestore read failed, the user IS
                            // authenticated – proceed to the home screen
                            callback.onSuccess();
                            return;
                        }

                        DocumentSnapshot snapshot = docTask.getResult();

                        if (snapshot != null && snapshot.exists()) {
                            // Document exists – nothing extra to do
                            Log.d(TAG, "login: user document found");
                            callback.onSuccess();
                        } else {
                            // Document does NOT exist – auto-create it
                            Log.w(TAG, "login: user document missing, creating one");
                            createMissingUserDocument(firebaseUser, callback);
                        }
                    });
                });
    }

    /**
     * Returns the currently signed-in {@link FirebaseUser}, or {@code null}
     * if no user is signed in.
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Signs the current user out of Firebase Auth.
     */
    public void signOut() {
        auth.signOut();
    }

    // ── Private helpers ─────────────────────────────────────────────


    private void createMissingUserDocument(@NonNull FirebaseUser firebaseUser,
                                           @NonNull AuthCallback callback) {

        String uid   = firebaseUser.getUid();
        String email = firebaseUser.getEmail() != null
                ? firebaseUser.getEmail() : "";
        String name  = firebaseUser.getDisplayName() != null
                ? firebaseUser.getDisplayName() : "";

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid",                  uid);
        userData.put("email",                email);
        userData.put("fullName",             name);
        userData.put("role",                 "student");
        userData.put("careerProfilesCount",  0);
        userData.put("createdAt",            FieldValue.serverTimestamp());

        db.collection(COLLECTION_USERS)
                .document(uid)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "createMissingUserDocument: document created");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    // Non-fatal: user is authenticated, just the doc write failed.
                    // We still let them into the app.
                    Log.e(TAG, "createMissingUserDocument: failed", e);
                    callback.onSuccess();
                });
    }
}
