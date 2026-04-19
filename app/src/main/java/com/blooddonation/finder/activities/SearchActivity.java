package com.blooddonation.finder.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.adapters.DonorAdapter;
import com.blooddonation.finder.databinding.ActivitySearchBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;
import com.blooddonation.finder.utils.LocationUtils;
import com.blooddonation.finder.utils.BloodCompatibilityUtils;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;
    private DonorAdapter donorAdapter;
    private List<Donor> donorList = new ArrayList<>();
    private double searchLat = 0, searchLng = 0;
    private static final int LOCATION_PERMISSION_REQUEST = 1002;

    private final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
    private final String[] RADII = {"5 km", "10 km", "25 km", "50 km"};
    private final double[] RADIUS_VALUES = {5, 10, 25, 50};

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupSpinners();
        setupRecyclerView();

        binding.btnUseMyLocation.setOnClickListener(v -> requestLocation());
        binding.btnSearch.setOnClickListener(v -> searchDonors());
        binding.btnViewMap.setOnClickListener(v ->
            startActivity(new Intent(this, MapActivity.class)));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.search_donors_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        binding.spinnerBloodGroup.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, BLOOD_GROUPS));
        binding.spinnerRadius.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, RADII));
    }

    private void setupRecyclerView() {
        donorAdapter = new DonorAdapter(this, donorList);
        binding.recyclerDonors.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDonors.setAdapter(donorAdapter);
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
            return;
        }
        fetchLocation();
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                searchLat = location.getLatitude();
                searchLng = location.getLongitude();
                binding.tvLocationHint.setText("📍 Location set: " +
                    String.format("%.4f, %.4f", searchLat, searchLng));
                binding.tvLocationHint.setVisibility(View.VISIBLE);
            }
        });
    }

    private void searchDonors() {
        // Validation
        String city = binding.etCity.getText().toString().trim();
        if (city.isEmpty() && searchLat == 0) {
            Toast.makeText(this, getString(R.string.city_required), Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedBloodGroup = BLOOD_GROUPS[binding.spinnerBloodGroup.getSelectedItemPosition()];
        double selectedRadius = RADIUS_VALUES[binding.spinnerRadius.getSelectedItemPosition()];

        // Feature 1: Blood Compatibility Matching
        boolean compatMode = binding.checkCompatible.isChecked();
        List<String> groupsToSearch = compatMode
            ? BloodCompatibilityUtils.compatibleDonors(selectedBloodGroup)
            : java.util.Collections.singletonList(selectedBloodGroup);

        if (compatMode) {
            binding.tvResultCount.setText("Searching compatible types: " + String.join(", ", groupsToSearch));
            binding.tvResultCount.setVisibility(android.view.View.VISIBLE);
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvResultCount.setVisibility(View.GONE);
        donorList.clear();
        donorAdapter.notifyDataSetChanged();

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Query all compatible blood groups from Firebase
        final int[] pending = {groupsToSearch.size()};
        for (String group : groupsToSearch) {
            mDatabase.child("donors")
                .orderByChild("bloodGroup")
                .equalTo(group)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Donor donor = ds.getValue(Donor.class);
                            if (donor == null) continue;
                            if (donor.getUid().equals(currentUid)) continue;
                            if (!donor.isAvailable()) continue;

                            if (searchLat != 0 && searchLng != 0) {
                                double distance = LocationUtils.distanceKm(
                                    searchLat, searchLng, donor.getLatitude(), donor.getLongitude());
                                if (distance > selectedRadius) continue;
                            } else {
                                if (!donor.getCity().equalsIgnoreCase(city)) continue;
                            }
                            donorList.add(donor);
                        }
                        pending[0]--;
                        if (pending[0] == 0) {
                            binding.progressBar.setVisibility(View.GONE);
                            donorAdapter.notifyDataSetChanged();
                            binding.tvResultCount.setText(donorList.size() + " donor(s) found");
                            binding.tvResultCount.setVisibility(View.VISIBLE);
                            binding.tvNoResults.setVisibility(donorList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, "Search failed. Please retry.", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
