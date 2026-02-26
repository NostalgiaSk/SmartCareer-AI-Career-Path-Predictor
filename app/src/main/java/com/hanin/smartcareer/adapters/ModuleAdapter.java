package com.hanin.smartcareer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Module;

import java.util.List;


public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

   
    public interface OnModuleClickListener {
        void onModuleClicked(int position);
    }

    private final Context context;
    private final List<Module> modules;
    private final OnModuleClickListener listener;


    public ModuleAdapter(Context context, List<Module> modules,
                         OnModuleClickListener listener) {
        this.context  = context;
        this.modules  = modules;
        this.listener = listener;
    }

   
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_module, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Module module = modules.get(position);

        // Set title and duration
        holder.tvModuleTitle.setText(module.getTitle());

        String duration = module.getDuration();
        if (duration != null && !duration.isEmpty()) {
            holder.tvDuration.setText(
                    context.getString(R.string.module_duration_prefix, duration));
            holder.tvDuration.setVisibility(View.VISIBLE);
        } else {
            holder.tvDuration.setVisibility(View.GONE);
        }

        // ── STATE 1: LOCKED ─────────────────────────────────────────
        if (module.isLocked()) {
            // Greyed-out appearance
            holder.ivLock.setVisibility(View.VISIBLE);
            holder.tvModuleNumber.setVisibility(View.GONE);
            holder.ivCheck.setVisibility(View.GONE);

            holder.tvModuleTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_text_hint));
            holder.tvDuration.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_text_hint));

            // Status badge
            holder.tvStatus.setText(R.string.module_locked);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_locked));

            // Hide quiz score
            holder.tvQuizScore.setVisibility(View.GONE);

            // Set locked card alpha
            holder.cardModule.setAlpha(0.6f);

            // Not clickable
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);

        // ── STATE 2: COMPLETED ──────────────────────────────────────
        } else if (module.isCompleted()) {
            // Green check appearance
            holder.ivLock.setVisibility(View.GONE);
            holder.tvModuleNumber.setVisibility(View.GONE);
            holder.ivCheck.setVisibility(View.VISIBLE);

            holder.tvModuleTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_text_primary));
            holder.tvDuration.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_text_secondary));

            // Status badge
            holder.tvStatus.setText(R.string.module_passed);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_success));

            // Show quiz score
            if (module.getQuizScore() > 0) {
                holder.tvQuizScore.setText(
                        context.getString(R.string.module_score_label, module.getQuizScore()));
                holder.tvQuizScore.setVisibility(View.VISIBLE);
            } else {
                holder.tvQuizScore.setVisibility(View.GONE);
            }

            // Full alpha
            holder.cardModule.setAlpha(1.0f);

            // Clickable — can review module content
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onModuleClicked(holder.getAdapterPosition());
                }
            });

        // ── STATE 3: UNLOCKED (not yet completed) ───────────────────
        } else {
            // Active appearance with module number
            holder.ivLock.setVisibility(View.GONE);
            holder.tvModuleNumber.setVisibility(View.VISIBLE);
            holder.tvModuleNumber.setText(
                    String.valueOf(module.getOrderIndex() + 1));
            holder.ivCheck.setVisibility(View.GONE);

            holder.tvModuleTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_text_primary));
            holder.tvDuration.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_text_secondary));

            // Status badge
            holder.tvStatus.setText(R.string.module_start);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_primary));

            // Hide quiz score
            holder.tvQuizScore.setVisibility(View.GONE);

            // Full alpha
            holder.cardModule.setAlpha(1.0f);

            // Clickable — opens module content
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onModuleClicked(holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() { return modules.size(); }


    static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardModule;
        ImageView ivLock, ivCheck;
        TextView tvModuleNumber, tvModuleTitle, tvDuration, tvStatus, tvQuizScore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardModule     = itemView.findViewById(R.id.cardModule);
            ivLock         = itemView.findViewById(R.id.ivLock);
            ivCheck        = itemView.findViewById(R.id.ivCheck);
            tvModuleNumber = itemView.findViewById(R.id.tvModuleNumber);
            tvModuleTitle  = itemView.findViewById(R.id.tvModuleTitle);
            tvDuration     = itemView.findViewById(R.id.tvDuration);
            tvStatus       = itemView.findViewById(R.id.tvStatus);
            tvQuizScore    = itemView.findViewById(R.id.tvQuizScore);
        }
    }
}
