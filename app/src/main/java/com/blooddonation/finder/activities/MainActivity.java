package com.blooddonation.finder.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.databinding.ActivityMainBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.models.BloodRequest;
import com.blooddonation.finder.utils.LocaleHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Donor currentDonor;
    private Vibrator vibrator;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        loadUserProfile();
        loadLiveStats();
        setupClickListeners();
    }

    private void loadUserProfile() {
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("donors").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentDonor = snapshot.getValue(Donor.class);
                if (currentDonor != null) {
                    binding.tvWelcome.setText(
                        getString(R.string.hello_greeting).replace("!", ", " + currentDonor.getName() + "! 👋"));
                    binding.tvBloodGroup.setText(currentDonor.getBloodGroup());
                    binding.tvDonationCount.setText(String.valueOf(currentDonor.getDonationCount()));
                    binding.switchAvailability.setChecked(currentDonor.isAvailable());
                    if (currentDonor.isAdmin()) {
                        binding.btnAdmin.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void setupClickListeners() {
        binding.btnSearchDonors.setOnClickListener(v ->
            startActivity(new Intent(this, SearchActivity.class)));

        binding.btnPostRequest.setOnClickListener(v ->
            startActivity(new Intent(this, PostRequestActivity.class)));

        binding.btnMapView.setOnClickListener(v ->
            startActivity(new Intent(this, MapActivity.class)));

        binding.btnDonationHistory.setOnClickListener(v ->
            startActivity(new Intent(this, DonationHistoryActivity.class)));

        binding.btnProfile.setOnClickListener(v ->
            startActivity(new Intent(this, ProfileActivity.class)));

        binding.btnNotifications.setOnClickListener(v ->
            startActivity(new Intent(this, NotificationsActivity.class)));

        binding.btnAdmin.setOnClickListener(v ->
            startActivity(new Intent(this, AdminActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Feature 3: SOS Emergency Mode
        binding.btnSOS.setOnClickListener(v -> {
            if (currentDonor == null) {
                Toast.makeText(this, "Profile not loaded yet, please wait.", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                .setTitle("🆘 SOS Emergency")
                .setMessage("This will broadcast an URGENT blood request to ALL donors near you matching your blood type (" + currentDonor.getBloodGroup() + "). Continue?")
                .setPositiveButton("BROADCAST NOW", (d, w) -> triggerSOSEmergency())
                .setNegativeButton("Cancel", null)
                .show();
        });

        binding.switchAvailability.setOnCheckedChangeListener((btn, isChecked) -> {
            String uid = mAuth.getCurrentUser().getUid();
            mDatabase.child("donors").child(uid).child("available").setValue(isChecked);

            // Update status text
            binding.tvAvailabilityStatus.setText(
                isChecked ? getString(R.string.available_to_donate) : getString(R.string.not_available_to_donate));

            // 🎬 Play Lottie pulse animation
            LottieAnimationView togglePulse = binding.lottieTogglePulse;
            togglePulse.cancelAnimation();
            togglePulse.setProgress(0f);
            togglePulse.playAnimation();

            // 📳 Haptic feedback
            triggerHapticFeedback(isChecked);
        });
    }

    // ─── Feature 2: Live Dashboard Stats ───────────────────────────────────
    private void loadLiveStats() {
        // Total donors — live
        mDatabase.child("donors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long total = 0, available = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    total++;
                    Boolean avail = ds.child("available").getValue(Boolean.class);
                    if (Boolean.TRUE.equals(avail)) available++;
                }
                binding.tvStatTotalDonors.setText(String.valueOf(total));
                binding.tvStatAvailable.setText(String.valueOf(available));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Open requests — live
        mDatabase.child("blood_requests").orderByChild("status").equalTo("OPEN")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    binding.tvStatOpenRequests.setText(String.valueOf(snapshot.getChildrenCount()));
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }

    // ─── Feature 3: SOS Emergency Broadcast ──────────────────────────────────
    private void triggerSOSEmergency() {
        if (currentDonor == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        BloodRequest sos = new BloodRequest(
            uid,
            currentDonor.getName(),
            currentDonor.getPhone(),
            currentDonor.getBloodGroup(),
            currentDonor.getCity(),
            currentDonor.getLatitude(),
            currentDonor.getLongitude(),
            "SOS Emergency",
            "CRITICAL"
        );
        sos.setNotes("⚠️ SOS — Emergency broadcast from app");
        String key = mDatabase.child("blood_requests").push().getKey();
        sos.setRequestId(key);
        mDatabase.child("blood_requests").child(key).setValue(sos)
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "🆘 SOS Broadcast sent! Donors are being notified.", Toast.LENGTH_LONG).show();
                // Haptic for emergency
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(
                            new long[]{0, 200, 100, 200, 100, 400}, -1));
                    }
                }
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "SOS Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Double-tap haptic for turning ON (feels like a confirmation).
     * Single short for turning OFF.
     */
    private void triggerHapticFeedback(boolean isAvailable) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isAvailable) {
                // Two taps — "confirmed!" feel
                VibrationEffect effect = VibrationEffect.createWaveform(
                    new long[]{0, 60, 80, 60}, -1);
                vibrator.vibrate(effect);
            } else {
                // Single short — "deactivated"
                VibrationEffect effect = VibrationEffect.createOneShot(
                    80, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            }
        } else {
            // Pre-Oreo fallback
            vibrator.vibrate(isAvailable ? new long[]{0, 60, 80, 60} : new long[]{0, 80}, -1);
        }
    }
}
