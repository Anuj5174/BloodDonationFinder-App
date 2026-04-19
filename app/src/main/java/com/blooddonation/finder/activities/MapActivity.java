package com.blooddonation.finder.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.blooddonation.finder.R;
import com.blooddonation.finder.databinding.ActivityMapBinding;
import com.blooddonation.finder.models.Donor;
import com.blooddonation.finder.utils.LocaleHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapBinding binding;
    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1003;
    // The same Maps API key declared in AndroidManifest meta-data
    private static final String MAPS_API_KEY = "AIzaSyB-i-vcorc-qkOd9gx9KfTX2FmoZIe1WO0";

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
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

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

        // Feature 5: Nearby Blood Banks
        binding.fabBloodBanks.setOnClickListener(v -> fetchNearbyBloodBanks());

        binding.fabLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 14f));
                    }
                });
            }
        });

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

    // ─── Feature 5: Nearby Blood Banks via Places API ───────────────────────
    private void fetchNearbyBloodBanks() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "🏥 Finding nearby blood banks...", Toast.LENGTH_SHORT).show();
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
                return;
            }
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            String urlStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + lat + "," + lng
                + "&radius=5000"
                + "&keyword=blood+bank"
                + "&key=" + MAPS_API_KEY;

            new Thread(() -> {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject resp = new JSONObject(sb.toString());
                    JSONArray results = resp.getJSONArray("results");

                    runOnUiThread(() -> {
                        try {
                            int count = 0;
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject place = results.getJSONObject(i);
                                String name = place.getString("name");
                                JSONObject loc = place.getJSONObject("geometry").getJSONObject("location");
                                double pLat = loc.getDouble("lat");
                                double pLng = loc.getDouble("lng");
                                mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(pLat, pLng))
                                    .title("🏥 " + name)
                                    .snippet("Blood Bank / Hospital")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                                count++;
                            }
                            Toast.makeText(this, count + " blood bank(s) found nearby!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(this, "Places API error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
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
