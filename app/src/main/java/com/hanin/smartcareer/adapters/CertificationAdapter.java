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
import com.hanin.smartcareer.activities.CertificationDetailActivity;
import com.hanin.smartcareer.models.Certification;

import java.util.List;


public class CertificationAdapter extends RecyclerView.Adapter<CertificationAdapter.CertViewHolder> {

    private final List<Certification> certifications;

    public CertificationAdapter(List<Certification> certifications) {
        this.certifications = certifications;
    }


    static class CertViewHolder extends RecyclerView.ViewHolder {

        final TextView tvCertName;
        final TextView tvProvider;
        final TextView tvDifficulty;
        final Button   btnStartLearning;

        CertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertName       = itemView.findViewById(R.id.tvCertName);
            tvProvider       = itemView.findViewById(R.id.tvProvider);
            tvDifficulty     = itemView.findViewById(R.id.tvDifficulty);
            btnStartLearning = itemView.findViewById(R.id.btnStartLearning);
        }
    }

    // ── Adapter overrides ───────────────────────────────────────────

    @NonNull
    @Override
    public CertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_certification, parent, false);
        return new CertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertViewHolder holder, int position) {
        Certification cert = certifications.get(position);

        holder.tvCertName.setText(cert.getCertName());
        holder.tvDifficulty.setText(cert.getDifficulty());

        // Format provider as "Offered by {provider}"
        String providerText = "Offered by " + cert.getProvider();
        holder.tvProvider.setText(providerText);

        // Navigate to CertificationDetailActivity with certification data
        holder.btnStartLearning.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, CertificationDetailActivity.class);
            intent.putExtra("certificationName", cert.getCertName());
            intent.putExtra("provider", cert.getProvider());
            intent.putExtra("level", cert.getDifficulty());
            intent.putExtra("progress", 0);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return certifications != null ? certifications.size() : 0;
    }
}
