package com.hanin.smartcareer.models;

import com.google.firebase.Timestamp;

/**
 * Model class representing a user document in the Firestore "users" collection.
 * <p>
 * Each document is keyed by the Firebase Authentication UID and stores
 * essential profile metadata created at registration time.
 * <p>
 * <b>Firestore path:</b> {@code users/{uid}}
 */
public class User {

    // ── Fields ───────────────────────────────────────────────────────

    /** Firebase Authentication UID (also used as the Firestore document ID). */
    private String uid;

    /** The user's email address (from FirebaseAuth). */
    private String email;

    /** The user's full name entered during registration. */
    private String fullName;

    /** Role assigned at registration. Defaults to "student". */
    private String role;

    /** Number of career profiles the user has created. Starts at 0. */
    private int careerProfilesCount;

    /** Server-side timestamp indicating when the account was created. */
    private Timestamp createdAt;

    // ── Constructors ─────────────────────────────────────────────────

    /**
     * No-argument constructor required by Firestore for automatic
     * deserialization of documents into Java objects.
     */
    public User() {
        // Required empty constructor for Firestore
    }

    /**
     * Convenience constructor used after a successful Firebase Auth registration.
     *
     * @param uid       Firebase Authentication UID.
     * @param email     User's email address.
     * @param fullName  User's full name.
     * @param role      Role string (e.g., "student").
     * @param careerProfilesCount Initial career profile count (typically 0).
     */
    public User(String uid, String email, String fullName, String role, int careerProfilesCount) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.careerProfilesCount = careerProfilesCount;
        // createdAt is set via FieldValue.serverTimestamp() when writing to Firestore,
        // so it is intentionally left null here.
    }

    // ── Getters & Setters ────────────────────────────────────────────
    // (Required by Firestore for serialization / deserialization)

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getCareerProfilesCount() {
        return careerProfilesCount;
    }

    public void setCareerProfilesCount(int careerProfilesCount) {
        this.careerProfilesCount = careerProfilesCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
