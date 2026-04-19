package com.blooddonation.finder.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.blooddonation.finder.R;
import com.blooddonation.finder.databinding.ActivityRegisterBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = 0, userLng = 0;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup blood group spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, BLOOD_GROUPS);
        binding.spinnerBloodGroup.setAdapter(adapter);

        binding.btnGetLocation.setOnClickListener(v -> requestLocation());
        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
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
                userLat = location.getLatitude();
                userLng = location.getLongitude();
                binding.tvLocationStatus.setText("📍 Location captured: " +
                    String.format("%.4f, %.4f", userLat, userLng));
                binding.tvLocationStatus.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Unable to get location. Please enter city manually.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();
        String bloodGroup = BLOOD_GROUPS[binding.spinnerBloodGroup.getSelectedItemPosition()];

        if (TextUtils.isEmpty(name)) { binding.etName.setError(getString(R.string.required_field)); return; }
        if (TextUtils.isEmpty(email)) { binding.etEmail.setError(getString(R.string.required_field)); return; }
        if (TextUtils.isEmpty(phone)) { binding.etPhone.setError(getString(R.string.required_field)); return; }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.etPassword.setError(getString(R.string.required_field)); return;
        }
        if (TextUtils.isEmpty(city)) { binding.etCity.setError(getString(R.string.required_field)); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = mAuth.getCurrentUser().getUid();
                    Donor donor = new Donor(uid, name, email, phone, bloodGroup, city, userLat, userLng);
                    mDatabase.child("donors").child(uid).setValue(donor)
                        .addOnCompleteListener(dbTask -> {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnRegister.setEnabled(true);
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to save profile.", Toast.LENGTH_SHORT).show();
                            }
                        });
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    Toast.makeText(this,
                        "Registration failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        }
    }
}
