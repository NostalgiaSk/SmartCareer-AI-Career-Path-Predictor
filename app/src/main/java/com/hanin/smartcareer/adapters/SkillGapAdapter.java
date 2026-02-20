package com.hanin.smartcareer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.SkillGap;

import java.util.List;

/**
 * Adapter for the "Skills You Need to Improve" RecyclerView.
 * Binds {@link SkillGap} objects to {@code item_skill_gap.xml} cards.
 */
public class SkillGapAdapter extends RecyclerView.Adapter<SkillGapAdapter.SkillGapViewHolder> {

    private final List<SkillGap> skillGaps;

    public SkillGapAdapter(List<SkillGap> skillGaps) {
        this.skillGaps = skillGaps;
    }

    // ── ViewHolder ──────────────────────────────────────────────────

    static class SkillGapViewHolder extends RecyclerView.ViewHolder {

        final TextView tvSkillName;
        final TextView tvExplanation;

        SkillGapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSkillName   = itemView.findViewById(R.id.tvSkillName);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
        }
    }

    // ── Adapter overrides ───────────────────────────────────────────

    @NonNull
    @Override
    public SkillGapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_skill_gap, parent, false);
        return new SkillGapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillGapViewHolder holder, int position) {
        SkillGap item = skillGaps.get(position);
        holder.tvSkillName.setText(item.getSkillName());
        holder.tvExplanation.setText(item.getExplanation());
    }

    @Override
    public int getItemCount() {
        return skillGaps != null ? skillGaps.size() : 0;
    }
}
