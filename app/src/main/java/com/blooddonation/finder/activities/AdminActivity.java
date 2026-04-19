package com.blooddonation.finder.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.adapters.AdminDonorAdapter;
import com.blooddonation.finder.databinding.ActivityAdminBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private DatabaseReference mDatabase;
    private AdminDonorAdapter adapter;
    private List<Donor> allDonors = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.admin_panel);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new AdminDonorAdapter(this, allDonors);
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerUsers.setAdapter(adapter);

        loadStats();
        loadAllDonors();
    }

    private void loadStats() {
        mDatabase.child("donors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long total = snapshot.getChildrenCount();
                long available = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Donor d = ds.getValue(Donor.class);
                    if (d != null && d.isAvailable()) available++;
                }
                binding.tvTotalUsers.setText(getString(R.string.total_donors_label)
                    .replace("--", String.valueOf(total)));
                binding.tvAvailableDonors.setText(getString(R.string.available_now_label)
                    .replace("--", String.valueOf(available)));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        mDatabase.child("blood_requests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long open = 0, completed = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    BloodRequestStatus s = ds.getValue(BloodRequestStatus.class);
                    if (s != null) {
                        if ("OPEN".equals(s.status)) open++;
                        if ("COMPLETED".equals(s.status)) completed++;
                    }
                }
                binding.tvOpenRequests.setText(getString(R.string.open_requests_label)
                    .replace("--", String.valueOf(open)));
                binding.tvCompletedDonations.setText(getString(R.string.completed_label)
                    .replace("--", String.valueOf(completed)));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadAllDonors() {
        binding.progressBar.setVisibility(View.VISIBLE);
        mDatabase.child("donors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                allDonors.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Donor d = ds.getValue(Donor.class);
                    if (d != null) allDonors.add(d);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Lightweight inner class for status parsing
    static class BloodRequestStatus {
        public String status;
        public BloodRequestStatus() {}
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
