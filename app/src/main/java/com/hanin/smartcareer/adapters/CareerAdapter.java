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
 * RecyclerView adapter that binds a list of {@link Career} objects to
 * {@code item_career.xml} card layouts.
 *
 * Each card displays the career name, match score, short description,
 * and a button that navigates to {@code SkillGapActivity} with the
 * selected career data passed via Intent.
 */
public class CareerAdapter extends RecyclerView.Adapter<CareerAdapter.CareerViewHolder> {

    private final List<Career> careers;
    private final Context context;

    /**
     * @param context  Host activity context (used for Intent navigation).
     * @param careers  Dataset produced by the AI matching engine.
     */
    public CareerAdapter(Context context, List<Career> careers) {
        this.context = context;
        this.careers = careers;
    }

    // ── ViewHolder ──────────────────────────────────────────────────

    static class CareerViewHolder extends RecyclerView.ViewHolder {

        final TextView tvCareerName;
        final TextView tvMatchScore;
        final TextView tvDescription;
        final Button   btnViewSkillGap;

        CareerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCareerName    = itemView.findViewById(R.id.tvCareerName);
            tvMatchScore    = itemView.findViewById(R.id.tvMatchScore);
            tvDescription   = itemView.findViewById(R.id.tvDescription);
            btnViewSkillGap = itemView.findViewById(R.id.btnViewSkillGap);
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

        holder.tvCareerName.setText(career.getCareerName());
        holder.tvDescription.setText(career.getDescription());

        // Format the match score as a styled percentage string
        String scoreText = career.getMatchScore() + " %";
        holder.tvMatchScore.setText(scoreText);

        // Navigate to SkillGapActivity when the user taps the button,
        // passing the full Career object for the detail screen to use.
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
}
