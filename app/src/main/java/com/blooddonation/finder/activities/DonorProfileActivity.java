package com.blooddonation.finder.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.databinding.ActivityDonorProfileBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;

public class DonorProfileActivity extends AppCompatActivity {

    private ActivityDonorProfileBinding binding;
    private Donor donor;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonorProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String donorId = getIntent().getStringExtra("donorId");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.contact_donor);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (donorId != null) {
            loadDonorProfile(donorId);
        }
    }

    private void loadDonorProfile(String donorId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
            .child("donors").child(donorId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                donor = snapshot.getValue(Donor.class);
                if (donor != null) {
                    displayDonorInfo();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DonorProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayDonorInfo() {
        binding.tvDonorName.setText(donor.getName());
        binding.tvBloodGroup.setText(donor.getBloodGroup());
        binding.tvCity.setText("📍 " + donor.getCity());
        binding.tvDonationCount.setText(
            String.format(getString(R.string.donations_count_format), donor.getDonationCount()));
        binding.tvAvailability.setText(
            donor.isAvailable() ? getString(R.string.available_status) : getString(R.string.not_available_status));

        if (donor.getProfileImageUrl() != null && !donor.getProfileImageUrl().isEmpty()) {
            Glide.with(this).load(donor.getProfileImageUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .into(binding.ivProfilePhoto);
        }

        binding.btnContact.setOnClickListener(v -> {
            if (donor.isAvailable()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + donor.getPhone()));
                startActivity(intent);
            } else {
                Toast.makeText(this, getString(R.string.not_available_status), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnWhatsapp.setOnClickListener(v -> {
            String url = "https://wa.me/" + donor.getPhone().replace("+", "");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
