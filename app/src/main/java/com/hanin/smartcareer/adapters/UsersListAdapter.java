package com.hanin.smartcareer.adapters;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.hanin.smartcareer.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UsersListAdapter
        extends RecyclerView.Adapter<UsersListAdapter.UserViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> users;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());



    public UsersListAdapter(Context context, List<Map<String, Object>> users) {
        this.context = context;
        this.users   = users;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, Object> user = users.get(position);

        String name = (String) user.get("fullName");
        holder.tvUserName.setText(name != null ? name : "Unknown");

        String email = (String) user.get("email");
        holder.tvUserEmail.setText(email != null ? email : "—");

        String role = (String) user.get("role");
        holder.tvUserRole.setText(
                context.getString(R.string.users_list_role_label,
                        role != null ? role : "student"));

        Timestamp ts = (Timestamp) user.get("createdAt");
        if (ts != null) {
            Date date = ts.toDate();
            holder.tvUserDate.setText(
                    context.getString(R.string.users_list_joined_label,
                            dateFormat.format(date)));
        } else {
            holder.tvUserDate.setText(
                    context.getString(R.string.users_list_joined_label, "N/A"));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // ── ViewHolder ──────────────────────────────────────────────────


    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        TextView tvUserEmail;
        TextView tvUserRole;
        TextView tvUserDate;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName  = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole  = itemView.findViewById(R.id.tvUserRole);
            tvUserDate  = itemView.findViewById(R.id.tvUserDate);
        }
    }
}
