package com.blooddonation.finder.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blooddonation.finder.R;
import com.blooddonation.finder.adapters.DonorAdapter;
import com.blooddonation.finder.databinding.ActivitySearchBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private DatabaseReference mDatabase;
    private List<Donor> donorList;
    private DonorAdapter adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference().child("donors");
        donorList = new ArrayList<>();
        
        setupSpinners();
        setupRecyclerView();

        binding.btnSearch.setOnClickListener(v -> performSearch());
        binding.btnViewMap.setOnClickListener(v -> 
            startActivity(new Intent(this, MapActivity.class)));
    }

    private void setupSpinners() {
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> bgAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bloodGroups);
        bgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerBloodGroup.setAdapter(bgAdapter);

        String[] radiusOptions = {"5 km", "10 km", "25 km", "50 km", "Anywhere"};
        ArrayAdapter<String> rAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, radiusOptions);
        rAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRadius.setAdapter(rAdapter);
    }

    private void setupRecyclerView() {
        adapter = new DonorAdapter(this, donorList);
        binding.recyclerDonors.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDonors.setAdapter(adapter);
    }

    private void performSearch() {
        String selectedBloodGroup = binding.spinnerBloodGroup.getSelectedItem().toString();
        String city = binding.etCity.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
        donorList.clear();
        adapter.notifyDataSetChanged();

        Query query = mDatabase.orderByChild("bloodGroup").equalTo(selectedBloodGroup);
        
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Donor donor = ds.getValue(Donor.class);
                    if (donor != null) {
                        if (city.isEmpty() || donor.getCity().equalsIgnoreCase(city)) {
                            donorList.add(donor);
                        }
                    }
                }

                if (donorList.isEmpty()) {
                    binding.tvNoResults.setVisibility(View.VISIBLE);
                    binding.tvResultCount.setVisibility(View.GONE);
                } else {
                    binding.tvResultCount.setText(getString(R.string.found_donors, donorList.size()));
                    binding.tvResultCount.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}
