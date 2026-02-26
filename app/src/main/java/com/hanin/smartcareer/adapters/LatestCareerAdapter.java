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
 * Adapter for "Latest Career Suggestions" on the Home screen.
 * Each card shows career name, match score, description,
 * a "View Details" button, a heart toggle, and a delete button.
 *
 * Heart toggle saves/unsaves to users/{uid}/saved_careers.
 * Delete removes from career_profiles/{profileId}/ai_suggestions.
 */
public class LatestCareerAdapter
        extends RecyclerView.Adapter<LatestCareerAdapter.ViewHolder> {

    private static final String TAG = "LatestCareerAdapter";

    private final Context context;
    private final List<Career> careers;
    private final Set<String> savedIds;     // Pre-loaded set of saved career IDs
    private final String profileId;         // Current profile's Firestore document ID
    private final FirebaseFirestore db;
    private final String uid;

    /**
     * @param context    Host activity context.
     * @param careers    List of career suggestions for the latest profile.
     * @param savedIds   Set of suggestion IDs the user has already saved.
     * @param profileId  Firestore document ID of the current career profile.
     */
    public LatestCareerAdapter(Context context, List<Career> careers,
                                Set<String> savedIds, String profileId) {
        this.context   = context;
        this.careers   = careers;
        this.savedIds  = savedIds;
        this.profileId = profileId;
        this.db        = FirebaseFirestore.getInstance();
        this.uid       = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_latest_career, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Career career = careers.get(position);

        // Bind text fields
        holder.tvCareerName.setText(career.getCareerName());
        holder.tvMatchScore.setText(career.getMatchScore() + "%");
        holder.tvDescription.setText(career.getDescription());

        // Set heart icon based on saved state
        String id = career.getSuggestionId();
        boolean isSaved = id != null && savedIds.contains(id);
        holder.btnHeart.setImageResource(
                isSaved ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

        // ── Heart toggle ────────────────────────────────────────────
        holder.btnHeart.setOnClickListener(v -> {
            if (uid == null || id == null) return;
            toggleSaveCareer(holder.btnHeart, career);
        });

        // ── Delete button ───────────────────────────────────────────
        holder.btnDelete.setOnClickListener(v -> {
            if (uid == null || id == null || profileId == null) return;
            deleteCareer(holder.getAdapterPosition(), id);
        });

        // ── View Details → SkillGapActivity ─────────────────────────
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, SkillGapActivity.class);
            intent.putExtra("career_data", career);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return careers.size(); }

    // ── ViewHolder ──────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvCareerName, tvMatchScore, tvDescription;
        Button      btnViewDetails;
        ImageButton btnHeart, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCareerName   = itemView.findViewById(R.id.tvCareerName);
            tvMatchScore   = itemView.findViewById(R.id.tvMatchScore);
            tvDescription  = itemView.findViewById(R.id.tvDescription);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnHeart       = itemView.findViewById(R.id.btnHeart);
            btnDelete      = itemView.findViewById(R.id.btnDelete);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // FIRESTORE OPERATIONS
    // ══════════════════════════════════════════════════════════════════

    /** Toggle save/unsave for a career in users/{uid}/saved_careers. */
    private void toggleSaveCareer(ImageButton btnHeart, Career career) {
        String id = career.getSuggestionId();
        boolean currentlySaved = savedIds.contains(id);

        // Scale animation on heart
        playHeartAnimation(btnHeart);

        if (currentlySaved) {
            // UNSAVE
            db.collection("users").document(uid)
                    .collection("saved_careers").document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        savedIds.remove(id);
                        btnHeart.setImageResource(R.drawable.ic_heart_outline);
                        Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to unsave", e);
                        Toast.makeText(context, "Error removing career", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // SAVE
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
                        Log.e(TAG, "Failed to save", e);
                        Toast.makeText(context, "Error saving career", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /** Delete a career from the ai_suggestions subcollection + RecyclerView. */
    private void deleteCareer(int position, String suggestionId) {
        if (position < 0 || position >= careers.size()) return;

        db.collection("career_profiles")
                .document(profileId)
                .collection("ai_suggestions")
                .document(suggestionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    careers.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, careers.size());
                    Toast.makeText(context, "Career deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete career", e);
                    Toast.makeText(context, "Error deleting career", Toast.LENGTH_SHORT).show();
                });
    }

    // ── Helpers ──────────────────────────────────────────────────────

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

    private void playHeartAnimation(ImageButton btn) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn, "scaleY", 1f, 1.3f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }
}
