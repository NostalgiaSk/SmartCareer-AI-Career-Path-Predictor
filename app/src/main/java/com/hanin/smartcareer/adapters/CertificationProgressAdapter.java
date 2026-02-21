package com.hanin.smartcareer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.CertificationProgress;

import java.util.List;

/**
 * Adapter for the "Certifications In Progress" section on the Home
 * dashboard. Each card shows certification name, provider, percentage
 * text, and a horizontal ProgressBar.
 */
public class CertificationProgressAdapter
        extends RecyclerView.Adapter<CertificationProgressAdapter.ViewHolder> {

    private final Context context;
    private final List<CertificationProgress> certifications;

    public CertificationProgressAdapter(Context context,
                                        List<CertificationProgress> certifications) {
        this.context        = context;
        this.certifications = certifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_certification_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CertificationProgress cert = certifications.get(position);

        holder.tvCertName.setText(cert.getCertName());
        holder.tvPercentage.setText(cert.getProgressPercent() + "%");
        holder.tvProvider.setText("by " + cert.getProvider());
        holder.progressBar.setProgress(cert.getProgressPercent());
    }

    @Override
    public int getItemCount() { return certifications.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCertName, tvPercentage, tvProvider;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertName  = itemView.findViewById(R.id.tvCertName);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            tvProvider  = itemView.findViewById(R.id.tvProvider);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
