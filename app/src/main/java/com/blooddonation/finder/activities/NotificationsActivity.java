package com.blooddonation.finder.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.adapters.RequestAdapter;
import com.blooddonation.finder.databinding.ActivityNotificationsBinding;
import com.blooddonation.finder.models.BloodRequest;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private DatabaseReference mDatabase;
    private RequestAdapter requestAdapter;
    private List<BloodRequest> requestList = new ArrayList<>();
    private Donor currentDonor;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.notifications_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        requestAdapter = new RequestAdapter(this, requestList, this::onAcceptRequest);
        binding.recyclerRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRequests.setAdapter(requestAdapter);

        loadCurrentUser();
    }

    private void loadCurrentUser() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("donors").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentDonor = snapshot.getValue(Donor.class);
                loadRequests();
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadRequests() {
        if (currentDonor == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("blood_requests")
            .orderByChild("bloodGroup")
            .equalTo(currentDonor.getBloodGroup())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    binding.progressBar.setVisibility(View.GONE);
                    requestList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        BloodRequest req = ds.getValue(BloodRequest.class);
                        if (req != null && "OPEN".equals(req.getStatus())) {
                            if (!req.getRequesterId().equals(currentDonor.getUid())) {
                                requestList.add(req);
                            }
                        }
                    }
                    requestAdapter.notifyDataSetChanged();
                    binding.tvNoRequests.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
    }

    private void onAcceptRequest(BloodRequest request) {
        mDatabase.child("blood_requests").child(request.getRequestId())
            .child("status").setValue("ACCEPTED");
        mDatabase.child("blood_requests").child(request.getRequestId())
            .child("acceptedDonorId").setValue(currentDonor.getUid());

        Toast.makeText(this,
            "✅ You accepted the request! Contact: " + request.getRequesterPhone(),
            Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
