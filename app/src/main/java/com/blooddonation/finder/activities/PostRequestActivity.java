package com.blooddonation.finder.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.databinding.ActivityPostRequestBinding;
import com.blooddonation.finder.models.BloodRequest;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;

public class PostRequestActivity extends AppCompatActivity {

    private ActivityPostRequestBinding binding;
    private DatabaseReference mDatabase;
    private Donor currentDonor;

    private final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
    private String[] URGENCY_LEVELS;
    private final String[] URGENCY_VALUES = {"CRITICAL", "URGENT", "NORMAL"};

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private TextRecognizer textRecognizer;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Localized urgency levels
        URGENCY_LEVELS = new String[]{
            getString(R.string.urgency_critical),
            getString(R.string.urgency_urgent),
            getString(R.string.urgency_normal)
        };

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.post_request);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.spinnerBloodGroup.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, BLOOD_GROUPS));
        binding.spinnerUrgency.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_dropdown_item, URGENCY_LEVELS));

        loadCurrentUser();
        binding.btnPostRequest.setOnClickListener(v -> postRequest());

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        processImageForOCR(imageUri);
                    }
                }
            }
        );

        binding.btnScanDocument.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void loadCurrentUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("donors").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentDonor = snapshot.getValue(Donor.class);
                if (currentDonor != null) {
                    binding.etCity.setText(currentDonor.getCity());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void postRequest() {
        String hospital = binding.etHospital.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();
        String notes = binding.etNotes.getText().toString().trim();
        String bloodGroup = BLOOD_GROUPS[binding.spinnerBloodGroup.getSelectedItemPosition()];
        String urgency = URGENCY_VALUES[binding.spinnerUrgency.getSelectedItemPosition()];

        if (TextUtils.isEmpty(hospital)) { binding.etHospital.setError(getString(R.string.hospital_required)); return; }
        if (TextUtils.isEmpty(city)) { binding.etCity.setError(getString(R.string.city_required)); return; }
        if (currentDonor == null) { Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show(); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnPostRequest.setEnabled(false);

        BloodRequest request = new BloodRequest(
            currentDonor.getUid(),
            currentDonor.getName(),
            currentDonor.getPhone(),
            bloodGroup,
            city,
            currentDonor.getLatitude(),
            currentDonor.getLongitude(),
            hospital,
            urgency
        );
        request.setNotes(notes);

        String requestId = mDatabase.child("blood_requests").push().getKey();
        request.setRequestId(requestId);

        mDatabase.child("blood_requests").child(requestId).setValue(request)
            .addOnCompleteListener(task -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnPostRequest.setEnabled(true);

                if (task.isSuccessful()) {
                    // 🎬 Show Lottie success overlay
                    showSuccessAnimation();
                } else {
                    Toast.makeText(this, R.string.request_failed, Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Shows the full-screen success overlay with Lottie checkmark animation,
     * then auto-closes the activity after 2 seconds.
     */
    private void showSuccessAnimation() {
        binding.successOverlay.setVisibility(View.VISIBLE);

        LottieAnimationView lottieSuccess = binding.lottieSuccess;
        lottieSuccess.setProgress(0f);
        lottieSuccess.playAnimation();

        // Auto-dismiss after 2 seconds and close activity
        new Handler().postDelayed(() -> {
            finish();
        }, 2000);
    }

    private void processImageForOCR(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            Toast.makeText(this, "Scanning Document...", Toast.LENGTH_SHORT).show();
            textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String fullText = visionText.getText();
                    extractDetailsFromText(fullText);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "OCR Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractDetailsFromText(String text) {
        text = text.toUpperCase();
        for (int i = 0; i < BLOOD_GROUPS.length; i++) {
            if (text.contains(" " + BLOOD_GROUPS[i] + " ") || text.contains(BLOOD_GROUPS[i] + "\n")) {
                binding.spinnerBloodGroup.setSelection(i);
                break;
            }
        }
        
        if (text.contains("CRITICAL") || text.contains("EMERGENCY")) {
            binding.spinnerUrgency.setSelection(0);
        } else if (text.contains("URGENT")) {
            binding.spinnerUrgency.setSelection(1);
        }

        binding.etNotes.setText("Scanned Document Info:\n" + text);
        Toast.makeText(this, "Form pre-filled from prescription!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
