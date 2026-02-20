package com.hanin.smartcareer.firebase;

/**
 * Centralised helper for Firebase operations.
 *
 * When you integrate Firebase, move all authentication and Firestore
 * calls here instead of scattering them across individual activities.
 *
 * <h3>Suggested methods to implement:</h3>
 * <ul>
 *   <li>{@code signIn(String email, String password, OnCompleteListener)}
 *       – wraps {@code FirebaseAuth.signInWithEmailAndPassword}.</li>
 *   <li>{@code register(String email, String password, OnCompleteListener)}
 *       – wraps {@code FirebaseAuth.createUserWithEmailAndPassword}.</li>
 *   <li>{@code saveCareerProfile(String uid, Map profile, OnCompleteListener)}
 *       – writes to the {@code career_profiles} Firestore collection.</li>
 *   <li>{@code fetchCareerSuggestions(String uid, OnCompleteListener)}
 *       – queries the {@code career_suggestions} collection.</li>
 * </ul>
 */
public class FirebaseHelper {

    // TODO: Add a singleton instance or pass FirebaseFirestore / FirebaseAuth
    //       via the constructor when the SDK is added to the project.
    //
    // private final FirebaseAuth auth;
    // private final FirebaseFirestore db;
    //
    // public FirebaseHelper() {
    //     auth = FirebaseAuth.getInstance();
    //     db   = FirebaseFirestore.getInstance();
    // }
}
