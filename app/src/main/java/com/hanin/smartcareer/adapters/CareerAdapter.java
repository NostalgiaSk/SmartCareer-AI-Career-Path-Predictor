package com.hanin.smartcareer.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.activities.SkillGapActivity;
import com.hanin.smartcareer.models.Career;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RecyclerView adapter for the AI career suggestions screen.
 * Each card shows career name, match score, description,
 * a "View Skill Gap" button, and a heart toggle for saving.
 *
 * The heart icon toggles between outline (unsaved) and filled (saved).
 * Saving writes to users/{uid}/saved_careers in Firestore.
 */
public class CareerAdapter extends RecyclerView.Adapter<CareerAdapter.CareerViewHolder> {

    private static final String TAG = "CareerAdapter";

    private final List<Career> careers;
    private final Context context;
    private final Set<String> savedIds;   // Pre-loaded set of saved career IDs
    private final FirebaseFirestore db;
    private final String uid;

    /**
     * @param context   Host activity context.
     * @param careers   Dataset produced by the AI matching engine.
     * @param savedIds  Set of suggestion IDs the user has already saved.
     */
    public CareerAdapter(Context context, List<Career> careers, Set<String> savedIds) {
        this.context  = context;
        this.careers  = careers;
        this.savedIds = savedIds;
        this.db       = FirebaseFirestore.getInstance();
        this.uid      = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    // ── ViewHolder ──────────────────────────────────────────────────

    static class CareerViewHolder extends RecyclerView.ViewHolder {

        final TextView    tvCareerName;
        final TextView    tvMatchScore;
        final TextView    tvDescription;
        final Button      btnViewSkillGap;
        final ImageButton btnHeart;

        CareerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCareerName    = itemView.findViewById(R.id.tvCareerName);
            tvMatchScore    = itemView.findViewById(R.id.tvMatchScore);
            tvDescription   = itemView.findViewById(R.id.tvDescription);
            btnViewSkillGap = itemView.findViewById(R.id.btnViewSkillGap);
            btnHeart        = itemView.findViewById(R.id.btnHeart);
        }
    }

    // ── Adapter overrides ───────────────────────────────────────────

    @NonNull
    @Override
    public CareerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_career, parent, false);
        return new CareerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CareerViewHolder holder, int position) {
        Career career = careers.get(position);

        // Bind text fields
        holder.tvCareerName.setText(career.getCareerName());
        holder.tvDescription.setText(career.getDescription());
        holder.tvMatchScore.setText(career.getMatchScore() + " %");

        // Set heart icon based on saved state
        String id = career.getSuggestionId();
        boolean isSaved = id != null && savedIds.contains(id);
        holder.btnHeart.setImageResource(
                isSaved ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        // Heart toggle click listener
        holder.btnHeart.setOnClickListener(v -> {
            if (uid == null || id == null) return;
            toggleSaveCareer(holder.btnHeart, career);
        });

        // Navigate to SkillGapActivity
        holder.btnViewSkillGap.setOnClickListener(v -> {
            Intent intent = new Intent(context, SkillGapActivity.class);
            intent.putExtra("career_data", career);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return careers != null ? careers.size() : 0;
    }

    // ── Heart toggle logic ──────────────────────────────────────────

    /**
     * Saves or removes a career from the user's saved_careers subcollection.
     * Includes a scale animation on the heart icon.
     */
    private void toggleSaveCareer(ImageButton btnHeart, Career career) {
        String id = career.getSuggestionId();
        boolean currentlySaved = savedIds.contains(id);

        // Play scale animation on the heart icon
        playHeartAnimation(btnHeart);

        if (currentlySaved) {
            // ── UNSAVE: remove from Firestore ────────────────────────
            db.collection("users").document(uid)
                    .collection("saved_careers").document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        savedIds.remove(id);
                        btnHeart.setImageResource(R.drawable.ic_heart_outline);
                        Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to unsave career", e);
                        Toast.makeText(context, "Error removing career", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // ── SAVE: add to Firestore ───────────────────────────────
            Map<String, Object> data = buildSaveData(career);
            db.collection("users").document(uid)
                    .collection("saved_careers").document(id)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        savedIds.add(id);
                        btnHeart.setImageResource(R.drawable.ic_heart_filled);
                        Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save career", e);
                        Toast.makeText(context, "Error saving career", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // ── Helper: build Firestore document data ───────────────────────

    private Map<String, Object> buildSaveData(Career career) {
        Map<String, Object> data = new HashMap<>();
        data.put("suggestionId", career.getSuggestionId());
        data.put("careerTitle", career.getCareerName());
        data.put("description", career.getDescription());
        data.put("matchScore", career.getMatchScore());
        data.put("requiredSkills", career.getSkillGaps());
        data.put("missingSkills", career.getSkillGaps());
        data.put("recommendedCertifications", career.getCertifications());
        data.put("savedAt", FieldValue.serverTimestamp());
        return data;
    }

    // ── Heart scale animation ───────────────────────────────────────

    private void playHeartAnimation(ImageButton btn) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn, "scaleY", 1f, 1.3f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }
}
