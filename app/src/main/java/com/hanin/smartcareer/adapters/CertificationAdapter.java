package com.hanin.smartcareer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanin.smartcareer.R;
import com.hanin.smartcareer.models.Certification;

import java.util.List;

/**
 * Adapter for the "Recommended Certifications" RecyclerView.
 * Binds {@link Certification} objects to {@code item_certification.xml} cards.
 */
public class CertificationAdapter extends RecyclerView.Adapter<CertificationAdapter.CertViewHolder> {

    private final List<Certification> certifications;

    public CertificationAdapter(List<Certification> certifications) {
        this.certifications = certifications;
    }

    // ── ViewHolder ──────────────────────────────────────────────────

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

        // Placeholder action — will open a learning URL in production
        holder.btnStartLearning.setOnClickListener(v ->
                Toast.makeText(v.getContext(),
                        "Coming soon: " + cert.getCertName(),
                        Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return certifications != null ? certifications.size() : 0;
    }
}
