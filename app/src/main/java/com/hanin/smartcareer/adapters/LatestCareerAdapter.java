package com.hanin.smartcareer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.activities.SkillGapActivity;
import com.hanin.smartcareer.models.Career;

import java.util.List;

/**
 * Adapter for the "Latest Career Suggestions" vertical RecyclerView
 * on the Home dashboard. Each card shows career name, match score,
 * short description, and a "View Details" button.
 */
public class LatestCareerAdapter
        extends RecyclerView.Adapter<LatestCareerAdapter.ViewHolder> {

    private final Context context;
    private final List<Career> careers;

    public LatestCareerAdapter(Context context, List<Career> careers) {
        this.context = context;
        this.careers = careers;
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

        holder.tvCareerName.setText(career.getCareerName());
        holder.tvMatchScore.setText(career.getMatchScore() + "%");
        holder.tvDescription.setText(career.getDescription());

        // Navigate to SkillGapActivity when "View Details" is tapped
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, SkillGapActivity.class);
            intent.putExtra("career_data", career);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return careers.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCareerName, tvMatchScore, tvDescription;
        Button btnViewDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCareerName  = itemView.findViewById(R.id.tvCareerName);
            tvMatchScore  = itemView.findViewById(R.id.tvMatchScore);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
