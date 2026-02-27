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
import com.hanin.smartcareer.activities.CertificationResultActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ObtainedCertificationsAdapter
        extends RecyclerView.Adapter<ObtainedCertificationsAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> certifications;

    public ObtainedCertificationsAdapter(Context context,
                                          List<Map<String, Object>> certifications) {
        this.context = context;
        this.certifications = certifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_obtained_certification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> cert = certifications.get(position);


        String title = cert.get("certificationName") != null
                ? cert.get("certificationName").toString() : "Certification";
        holder.tvCertTitle.setText(title);

 
        String dateStr = "—";
        Object completedAt = cert.get("completedAt");
        if (completedAt instanceof com.google.firebase.Timestamp) {
            Date date = ((com.google.firebase.Timestamp) completedAt).toDate();
            dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        }
        holder.tvCompletionDate.setText(
                context.getString(R.string.obtained_cert_date, dateStr));

        // Score
        Object scoreObj = cert.get("totalScore");
        int score = 0;
        if (scoreObj instanceof Number) {
            score = ((Number) scoreObj).intValue();
        }
        holder.tvScore.setText(context.getString(R.string.obtained_cert_score, score));


        final String finalTitle = title;
        final String finalDate = dateStr;
        final int finalScore = score;

        String provider = cert.get("provider") != null
                ? cert.get("provider").toString() : "";

        Object totalModObj = cert.get("totalModules");
        int totalModules = totalModObj instanceof Number
                ? ((Number) totalModObj).intValue() : 0;

        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, CertificationResultActivity.class);
            intent.putExtra("certTitle", finalTitle);
            intent.putExtra("completionDate", finalDate);
            intent.putExtra("finalScore", finalScore);
            intent.putExtra("provider", provider);
            intent.putExtra("totalModules", totalModules);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return certifications.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCertTitle, tvCompletionDate, tvScore;
        Button btnViewDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertTitle     = itemView.findViewById(R.id.tvCertTitle);
            tvCompletionDate = itemView.findViewById(R.id.tvCompletionDate);
            tvScore         = itemView.findViewById(R.id.tvScore);
            btnViewDetails  = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
