package com.example.mygooglemapmarkers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GoogleMap mMap;
    private GoogleApiClient mClient;
    private Geocoder mGeocoder;

    private TextView mTitle, mDetail;

    private final GoogleApiClient.ConnectionCallbacks callbacks = new GoogleApiClient.ConnectionCallbacks() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            try {
                LocationRequest mLocationRequest = new LocationRequest()
                        .setInterval(60)
                        .setFastestInterval(60)
                        .setMaxWaitTime(120)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(mClient,
                        mLocationRequest,
                        locationListener);

            } catch (SecurityException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            // การเชื่อมต่อหยุดชั่วขณะ
            Toast.makeText(getBaseContext(), "การเชื่อมต่อหยุดชั่วขณะ", Toast.LENGTH_SHORT).show();
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener onFails = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(getBaseContext(), "การเชื่อมต่อล้มเหลว", Toast.LENGTH_SHORT).show();
        }
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            mMap.clear();

            double lat = location.getLatitude();
            double lon = location.getLongitude();

            LatLng latLng = new LatLng(lat, lon);

            List<Address> addresses = null;
            try {
                addresses = mGeocoder.getFromLocation(lat, lon, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses.isEmpty()) {
                mMap.addMarker(new MarkerOptions()
                        .title("Unknown")
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_accessibility_new_white_20)));
            } else {
                Address address1 = addresses.get(0);
                String featureName = address1.getFeatureName();
                int numLine = address1.getMaxAddressLineIndex();
                String detail = "";
                for (int i = 0; i < numLine + 1; i++) {
                    detail += address1.getAddressLine(i);
                }

                MarkerOptions markerOptions = new MarkerOptions()
                        .snippet(detail)
                        .title(featureName)
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_accessibility_new_white_20));
                mMap.addMarker(markerOptions);

                mTitle.setText(featureName);
                mDetail.setText(detail);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = findViewById(R.id.tv_title);
        mDetail = findViewById(R.id.tv_detail);

        mClient = new GoogleApiClient.Builder(getBaseContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(callbacks)
                .addOnConnectionFailedListener(onFails)
                .build();

        mGeocoder = new Geocoder(getBaseContext());

        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(
                R.id.map_fragment
        );
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setMyLocationEnabled(true);
                    mMap.setBuildingsEnabled(true);
                    mMap.setIndoorEnabled(true);
                    mMap.setTrafficEnabled(true);

                    CameraPosition cameraPosition = mMap.getCameraPosition();
                    LatLng latLng = cameraPosition.target;

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

                    List<Address> addressList = null;
                    try {
                        addressList = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addressList.isEmpty()) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(latLng)
                                .title("Unknown");
                        mMap.addMarker(markerOptions);
                        return;
                    }
                    Address address1 = addressList.get(0);
                    String featureName = address1.getFeatureName();
                    int numLine = address1.getMaxAddressLineIndex();
                    String detail = "";
                    for (int i = 0; i < numLine + 1; i++) {
                        detail += address1.getAddressLine(i);
                    }

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .title(featureName)
                            .snippet(detail);
                    mMap.addMarker(markerOptions);

                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(@NonNull LatLng latLng) {

                        }
                    });
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mClient != null) {
            mClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient != null && mClient.isConnected()) {
            mClient.disconnect();
        }
    }
}