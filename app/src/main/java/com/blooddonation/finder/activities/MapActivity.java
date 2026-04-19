package com.blooddonation.finder.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.databinding.ActivityMapBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapBinding binding;
    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private static final int LOCATION_PERMISSION_REQUEST = 1003;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.view_on_map);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
        }

        loadDonorsOnMap();

        mMap.setOnMarkerClickListener(marker -> {
            String donorId = (String) marker.getTag();
            if (donorId != null) {
                Intent intent = new Intent(MapActivity.this, DonorProfileActivity.class);
                intent.putExtra("donorId", donorId);
                startActivity(intent);
            }
            return false;
        });
    }

    private void loadDonorsOnMap() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabase.child("donors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Donor donor = ds.getValue(Donor.class);
                    if (donor == null || donor.getUid().equals(currentUid)) continue;
                    if (donor.getLatitude() == 0 && donor.getLongitude() == 0) continue;

                    LatLng position = new LatLng(donor.getLatitude(), donor.getLongitude());
                    float markerColor = donor.isAvailable()
                        ? BitmapDescriptorFactory.HUE_RED
                        : BitmapDescriptorFactory.HUE_AZURE;

                    String snippet = donor.isAvailable()
                        ? getString(R.string.available) + " ✓"
                        : getString(R.string.not_available);

                    Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(donor.getName() + " (" + donor.getBloodGroup() + ")")
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

                    if (marker != null) {
                        marker.setTag(donor.getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapActivity.this, "Failed to load donors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
