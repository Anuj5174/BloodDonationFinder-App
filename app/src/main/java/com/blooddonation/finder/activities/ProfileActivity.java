package com.blooddonation.finder.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.blooddonation.finder.R;
import com.blooddonation.finder.databinding.ActivityProfileBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private Donor currentDonor;
    private Uri selectedImageUri;

    private final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};

    // Language options — display names and codes
    private String[] LANGUAGE_NAMES;
    private final String[] LANGUAGE_CODES = {LocaleHelper.LANG_ENGLISH, LocaleHelper.LANG_HINDI};

    private boolean isLanguageInitialized = false;

    private final ActivityResultLauncher<String> pickImageLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                binding.ivProfilePhoto.setImageURI(uri);
            }
        });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.my_profile);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Blood group spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, BLOOD_GROUPS);
        binding.spinnerBloodGroup.setAdapter(adapter);

        // Language spinner
        LANGUAGE_NAMES = new String[]{getString(R.string.lang_english), getString(R.string.lang_hindi)};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, LANGUAGE_NAMES);
        binding.spinnerLanguage.setAdapter(langAdapter);

        // Set current language selection
        String currentLang = LocaleHelper.getSavedLanguage(this);
        for (int i = 0; i < LANGUAGE_CODES.length; i++) {
            if (LANGUAGE_CODES[i].equals(currentLang)) {
                binding.spinnerLanguage.setSelection(i);
                break;
            }
        }

        // Listen for language changes
        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isLanguageInitialized) {
                    isLanguageInitialized = true;
                    return; // Skip the initial trigger
                }
                String selectedLang = LANGUAGE_CODES[position];
                String currentLang = LocaleHelper.getSavedLanguage(ProfileActivity.this);
                if (!selectedLang.equals(currentLang)) {
                    LocaleHelper.setLocale(ProfileActivity.this, selectedLang);
                    Toast.makeText(ProfileActivity.this,
                        R.string.language_changed, Toast.LENGTH_SHORT).show();
                    // Restart the entire app flow to apply new locale
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.ivProfilePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void loadProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("donors").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentDonor = snapshot.getValue(Donor.class);
                if (currentDonor != null) {
                    binding.etName.setText(currentDonor.getName());
                    binding.etPhone.setText(currentDonor.getPhone());
                    binding.etCity.setText(currentDonor.getCity());

                    // Set spinner
                    for (int i = 0; i < BLOOD_GROUPS.length; i++) {
                        if (BLOOD_GROUPS[i].equals(currentDonor.getBloodGroup())) {
                            binding.spinnerBloodGroup.setSelection(i);
                            break;
                        }
                    }

                    if (currentDonor.getProfileImageUrl() != null) {
                        Glide.with(ProfileActivity.this)
                            .load(currentDonor.getProfileImageUrl())
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(binding.ivProfilePhoto);
                    }

                    binding.tvDonationCount.setText(currentDonor.getDonationCount() + " Donations");
                    binding.tvBadge.setText(currentDonor.getBadgeTitle());
                    binding.tvLoyaltyPoints.setText("⭐ " + currentDonor.getLoyaltyPoints() + " pts");
                    binding.tvEmail.setText(currentDonor.getEmail());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();
        String bloodGroup = BLOOD_GROUPS[binding.spinnerBloodGroup.getSelectedItemPosition()];

        if (TextUtils.isEmpty(name)) { binding.etName.setError(getString(R.string.required_field)); return; }
        if (TextUtils.isEmpty(phone)) { binding.etPhone.setError(getString(R.string.required_field)); return; }
        if (TextUtils.isEmpty(city)) { binding.etCity.setError(getString(R.string.required_field)); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (selectedImageUri != null) {
            StorageReference ref = mStorage.child("profile_photos/" + uid + ".jpg");
            ref.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateFirebase(uid, name, phone, city, bloodGroup, uri.toString());
                })
            ).addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(this, R.string.image_upload_failed, Toast.LENGTH_SHORT).show();
            });
        } else {
            String existingUrl = currentDonor != null ? currentDonor.getProfileImageUrl() : null;
            updateFirebase(uid, name, phone, city, bloodGroup, existingUrl);
        }
    }

    private void updateFirebase(String uid, String name, String phone, String city,
                                String bloodGroup, String imageUrl) {
        DatabaseReference ref = mDatabase.child("donors").child(uid);
        ref.child("name").setValue(name);
        ref.child("phone").setValue(phone);
        ref.child("city").setValue(city);
        ref.child("bloodGroup").setValue(bloodGroup);
        if (imageUrl != null) ref.child("profileImageUrl").setValue(imageUrl);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(ProfileActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
