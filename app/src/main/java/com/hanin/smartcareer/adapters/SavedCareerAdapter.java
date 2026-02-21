package com.hanin.smartcareer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Career;

import java.util.List;

/**
 * Adapter for the "Saved Careers" horizontal RecyclerView on the Home
 * dashboard. Displays compact cards showing career name and match score.
 */
public class SavedCareerAdapter
        extends RecyclerView.Adapter<SavedCareerAdapter.ViewHolder> {

    private final Context context;
    private final List<Career> savedCareers;

    public SavedCareerAdapter(Context context, List<Career> savedCareers) {
        this.context      = context;
        this.savedCareers = savedCareers;
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

        holder.tvCareerName.setText(career.getCareerName());
        holder.tvMatchScore.setText(career.getMatchScore() + "%");

        // Placeholder: tap to open career details
        holder.itemView.setOnClickListener(v ->
                Toast.makeText(context,
                        career.getCareerName() + " tapped",
                        Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() { return savedCareers.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCareerName, tvMatchScore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCareerName = itemView.findViewById(R.id.tvCareerName);
            tvMatchScore = itemView.findViewById(R.id.tvMatchScore);
        }
    }
}
