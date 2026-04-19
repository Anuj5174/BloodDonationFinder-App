package com.blooddonation.finder.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.adapters.HistoryAdapter;
import com.blooddonation.finder.databinding.ActivityDonationHistoryBinding;
import com.blooddonation.finder.models.DonationHistory;
import com.blooddonation.finder.utils.LocaleHelper;
import java.util.ArrayList;
import java.util.List;

public class DonationHistoryActivity extends AppCompatActivity {

    private ActivityDonationHistoryBinding binding;
    private DatabaseReference mDatabase;
    private HistoryAdapter historyAdapter;
    private List<DonationHistory> historyList = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonationHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.donation_history);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        historyAdapter = new HistoryAdapter(this, historyList);
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerHistory.setAdapter(historyAdapter);

        loadHistory();
    }

    private void loadHistory() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("donation_history").orderByChild("donorId").equalTo(uid)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    binding.progressBar.setVisibility(View.GONE);
                    historyList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        DonationHistory history = ds.getValue(DonationHistory.class);
                        if (history != null) historyList.add(history);
                    }
                    historyAdapter.notifyDataSetChanged();

                    if (historyList.isEmpty()) {
                        binding.tvNoHistory.setVisibility(View.VISIBLE);
                        binding.recyclerHistory.setVisibility(View.GONE);
                    } else {
                        binding.tvNoHistory.setVisibility(View.GONE);
                        binding.recyclerHistory.setVisibility(View.VISIBLE);
                        binding.tvTotalDonations.setText(
                            String.format(getString(R.string.total_donations_count), historyList.size()));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
