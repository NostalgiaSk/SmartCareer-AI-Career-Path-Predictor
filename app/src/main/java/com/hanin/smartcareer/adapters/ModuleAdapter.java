package com.hanin.smartcareer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Module;

import java.util.List;

/**
 * Adapter for the module list in CertificationDetailActivity.
 *
 * Each card shows the module title, its completion status, and a
 * "Mark as Completed" button.  When a module is marked complete the
 * adapter notifies the hosting activity via the {@link OnModuleCompletedListener}
 * callback so it can recalculate overall progress.
 */
public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

    // ── Callback interface ───────────────────────────────────────────

    /**
     * Listener invoked when a module is marked as completed.
     * The hosting activity implements this to recalculate progress.
     */
    public interface OnModuleCompletedListener {
        void onModuleCompleted(int position);
    }

    // ── Fields ───────────────────────────────────────────────────────

    private final Context context;
    private final List<Module> modules;
    private final OnModuleCompletedListener listener;

    public ModuleAdapter(Context context, List<Module> modules,
                         OnModuleCompletedListener listener) {
        this.context  = context;
        this.modules  = modules;
        this.listener = listener;
    }

    // ── Adapter overrides ────────────────────────────────────────────

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

        // Module title
        holder.tvModuleTitle.setText(module.getTitle());

        if (module.isCompleted()) {
            // ── Completed state ──────────────────────────────────
            holder.tvStatus.setText(R.string.cert_detail_completed);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_success));
            holder.ivCheck.setVisibility(View.VISIBLE);
            holder.btnMarkComplete.setVisibility(View.GONE);
        } else {
            // ── Pending state ────────────────────────────────────
            holder.tvStatus.setText(R.string.cert_detail_pending);
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.sc_text_hint));
            holder.ivCheck.setVisibility(View.GONE);
            holder.btnMarkComplete.setVisibility(View.VISIBLE);

            // Handle "Mark as Completed" click
            holder.btnMarkComplete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onModuleCompleted(holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() { return modules.size(); }

    // ── ViewHolder ───────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCheck;
        TextView tvModuleTitle, tvStatus;
        Button btnMarkComplete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCheck         = itemView.findViewById(R.id.ivCheck);
            tvModuleTitle   = itemView.findViewById(R.id.tvModuleTitle);
            tvStatus        = itemView.findViewById(R.id.tvStatus);
            btnMarkComplete = itemView.findViewById(R.id.btnMarkComplete);
        }
    }
}
