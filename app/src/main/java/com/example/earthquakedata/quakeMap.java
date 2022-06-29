package com.example.earthquakedata;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class quakeMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double lat=0;
    double lon=0;
    double mag;

    private static final String LOGTAG = "quakeMap";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_map);

        String latitude = getIntent().getStringExtra("latitude");
        lat = Double.parseDouble(latitude);
        String longitude = getIntent().getStringExtra("longitude");
        lon = Double.parseDouble(longitude);
        Log.e(LOGTAG,"the quake map received the coordinates "+latitude+" "+longitude);
        mag = getIntent().getDoubleExtra("mag",mag);
        Log.e(LOGTAG,"the the quakemap received the magnitude : "+mag);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng earthquake = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(earthquake).title("Mag: "+mag+" Co:"+lat+","+lon));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(earthquake));
    }
}