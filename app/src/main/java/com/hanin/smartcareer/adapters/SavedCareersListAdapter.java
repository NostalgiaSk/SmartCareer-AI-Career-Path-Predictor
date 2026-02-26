package com.hanin.smartcareer.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.activities.CareerDetailActivity;
import com.hanin.smartcareer.models.Career;

import java.util.List;


public class SavedCareersListAdapter
        extends RecyclerView.Adapter<SavedCareersListAdapter.ViewHolder> {

    private static final String TAG = "SavedCareersListAdapt";

    private final Context context;
    private final List<Career> savedCareers;
    private final FirebaseFirestore db;
    private final String uid;

    /**
     * @param context      Host activity context.
     * @param savedCareers List of saved career objects.
     */
    public SavedCareersListAdapter(Context context, List<Career> savedCareers) {
        this.context      = context;
        this.savedCareers = savedCareers;
        this.db           = FirebaseFirestore.getInstance();
        this.uid          = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse the item_latest_career layout for a consistent card look
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_latest_career, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Career career = savedCareers.get(position);

        // Bind text fields
        holder.tvCareerName.setText(career.getCareerName());
        holder.tvMatchScore.setText(career.getMatchScore() + "%");
        holder.tvDescription.setText(
                career.getDescription() != null ? career.getDescription() : "");

        // Heart is always filled — this is the saved careers list
        holder.btnHeart.setImageResource(R.drawable.ic_heart_filled);

        // ── Heart click = unsave (same as delete) ───────────────────
        holder.btnHeart.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos < 0 || adapterPos >= savedCareers.size()) return;
            playHeartAnimation(holder.btnHeart);
            deleteCareer(adapterPos, career.getSuggestionId());
        });

        // ── Delete click ────────────────────────────────────────────
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos < 0 || adapterPos >= savedCareers.size()) return;
            deleteCareer(adapterPos, career.getSuggestionId());
        });

        // ── View Details → navigates to CareerDetailActivity ────────
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, CareerDetailActivity.class);
            intent.putExtra("suggestion_id", career.getSuggestionId());
            context.startActivity(intent);
        });

        // ── Card click → same as View Details ───────────────────────
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CareerDetailActivity.class);
            intent.putExtra("suggestion_id", career.getSuggestionId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return savedCareers.size(); }

    // ── ViewHolder ──────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCareerName, tvMatchScore, tvDescription;
        android.widget.Button btnViewDetails;
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

    /**
     * Deletes a saved career from Firestore and removes it from the list.
     *
     * @param position     Adapter position of the item to remove.
     * @param suggestionId Firestore document ID in saved_careers.
     */
    private void deleteCareer(int position, String suggestionId) {
        if (uid == null || suggestionId == null) return;

        db.collection("users").document(uid)
                .collection("saved_careers").document(suggestionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from list and update RecyclerView
                    savedCareers.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, savedCareers.size());
                    Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete saved career", e);
                    Toast.makeText(context, "Error removing career", Toast.LENGTH_SHORT).show();
                });
    }

    // ── Heart animation ─────────────────────────────────────────────

    private void playHeartAnimation(ImageButton btn) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btn, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btn, "scaleY", 1f, 1.3f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }
}
