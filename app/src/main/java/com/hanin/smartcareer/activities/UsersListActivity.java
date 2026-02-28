package com.hanin.smartcareer.activities;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hanin.smartcareer.R;
import com.hanin.smartcareer.adapters.UsersListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UsersListActivity extends AppCompatActivity {

    private static final String TAG = "UsersListActivity";

    private RecyclerView rvUsers;
    private ProgressBar progressUsers;
    private TextView tvEmptyUsers;

    private List<Map<String, Object>> usersList;
    private UsersListAdapter adapter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        fetchAllUsers();
    }


    private void initViews() {
        rvUsers      = findViewById(R.id.rvUsers);
        progressUsers = findViewById(R.id.progressUsers);
        tvEmptyUsers = findViewById(R.id.tvEmptyUsers);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        usersList = new ArrayList<>();
        adapter   = new UsersListAdapter(this, usersList);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    /**
     * Fetches all documents from the "users" Firestore collection.
     */
    private void fetchAllUsers() {
        progressUsers.setVisibility(View.VISIBLE);
        tvEmptyUsers.setVisibility(View.GONE);
        rvUsers.setVisibility(View.GONE);

        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    usersList.clear();

                    // Convert each Firestore document to a Map
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("fullName", doc.getString("fullName"));
                        user.put("email",    doc.getString("email"));
                        user.put("role",     doc.getString("role"));
                        user.put("createdAt", doc.getTimestamp("createdAt"));
                        usersList.add(user);
                    }

                    // Notify the adapter of the new data
                    adapter.notifyDataSetChanged();
                    progressUsers.setVisibility(View.GONE);

                    if (usersList.isEmpty()) {
                        // No users found — show empty state
                        tvEmptyUsers.setVisibility(View.VISIBLE);
                        rvUsers.setVisibility(View.GONE);
                    } else {
                        // Users found — show the list
                        tvEmptyUsers.setVisibility(View.GONE);
                        rvUsers.setVisibility(View.VISIBLE);
                    }

                    Log.d(TAG, "Fetched " + usersList.size() + " users");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch users", e);
                    progressUsers.setVisibility(View.GONE);
                    tvEmptyUsers.setVisibility(View.VISIBLE);
                    rvUsers.setVisibility(View.GONE);
                    Toast.makeText(this,
                            R.string.users_list_error,
                            Toast.LENGTH_SHORT).show();
                });
    }
}
