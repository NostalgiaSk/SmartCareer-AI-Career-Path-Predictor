package com.hanin.smartcareer.adapters;

import android.content.Context;
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
import com.hanin.smartcareer.models.Career;

import java.util.List;


public class SavedCareerAdapter
        extends RecyclerView.Adapter<SavedCareerAdapter.ViewHolder> {

    private static final String TAG = "SavedCareerAdapter";

    private final Context context;
    private final List<Career> savedCareers;
    private final FirebaseFirestore db;
    private final String uid;

    public SavedCareerAdapter(Context context, List<Career> savedCareers) {
        this.context      = context;
        this.savedCareers = savedCareers;
        this.db           = FirebaseFirestore.getInstance();
        this.uid          = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_saved_career, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Career career = savedCareers.get(position);

        // Bind text fields
        holder.tvCareerName.setText(career.getCareerName());
        holder.tvMatchScore.setText(career.getMatchScore() + "%");

        // ── Delete button ───────────────────────────────────────────
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos < 0 || adapterPos >= savedCareers.size()) return;

            String id = career.getSuggestionId();
            if (uid == null || id == null) return;

            // Delete from Firestore
            db.collection("users").document(uid)
                    .collection("saved_careers").document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove from list and update RecyclerView
                        savedCareers.remove(adapterPos);
                        notifyItemRemoved(adapterPos);
                        notifyItemRangeChanged(adapterPos, savedCareers.size());
                        Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete saved career", e);
                        Toast.makeText(context, "Error removing career", Toast.LENGTH_SHORT).show();
                    });
        });

        // Placeholder: tap card to open career details
        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context,
                        career.getCareerName() + " tapped",
                        Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() { return savedCareers.size(); }

    // ── ViewHolder ──────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvCareerName, tvMatchScore;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCareerName = itemView.findViewById(R.id.tvCareerName);
            tvMatchScore = itemView.findViewById(R.id.tvMatchScore);
            btnDelete    = itemView.findViewById(R.id.btnDelete);
        }
    }
}
