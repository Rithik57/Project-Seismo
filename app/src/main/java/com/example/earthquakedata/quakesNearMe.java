package com.example.earthquakedata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class quakesNearMe extends AppCompatActivity {
    //the request url should modify https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&limit=100&latitude=29.288670&longitude=76.039848&maxradius=20

    // initializing
    // FusedLocationProviderClient
    // object
    FusedLocationProviderClient mFusedLocationClient;

    String Location;
    double latitude;
    double longitude;
    int PERMISSION_ID = 44;

    //url to be modified
    private String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&limit=100";

    private static final String LOGTAG = "quakesNearMe";

    //counter to avoid multiple runners
    private int count=0;

    //list view for nearby earthquakes
    ListView nearList;

    getQuakeInfo newRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quakes_near_me);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // method to get the location
        getLastLocation();

        //finding the listview
        nearList = (ListView)findViewById(R.id.nearList);
    }

    private void updateURL(){
        //only one asynctask
        if(count>0){
            return;
        }
        count++;

        //geocoder class to convert coordinates into address
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());    //getting a geocoder for the default locale settings
        try {
            //.getFromLocation method returns an array of possible addresses filtered by maxResults
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            Address obj = addresses.get(0);
            Location = obj.getAddressLine(0);
            Location = Location + obj.getCountryName();
            Log.e(LOGTAG,"the location was extracted as : "+Location);
            TextView userLocation = (TextView)findViewById(R.id.UserLocation);
            userLocation.setText(Location);

        } catch (IOException e) {
            Log.e(LOGTAG,"there was an error converting the coordinates into usable address");
            TextView userLocation = (TextView)findViewById(R.id.UserLocation);
            userLocation.setText("Unknown Location");
        }

        //updating the request URL
        USGS_REQUEST_URL = USGS_REQUEST_URL + "&latitude="+String.valueOf(latitude)+"&longitude="+String.valueOf(longitude);
        //maxradius from the current location in degrees
        USGS_REQUEST_URL = USGS_REQUEST_URL + "&maxradius=20";
        Log.e(LOGTAG,"the request URL is now : "+USGS_REQUEST_URL);

        //background thread to get the LIST
        newRunner = new getQuakeInfo();
        newRunner.execute(USGS_REQUEST_URL);

        //resetting the URL for the next background task;
        USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&limit=100";
    }

    private class getQuakeInfo extends AsyncTask<String,Integer,List<quakes>>{

        @Override
        protected List<quakes> doInBackground(String... strings) {
            String map_image_URL = "http://maps.google.com/maps/api/staticmap?center=" + String.valueOf(latitude) + "," + String.valueOf(longitude) + "&zoom=15&size=200x200&sensor=false&key=AIzaSyATUsXQ_7nGNgG1QoR0jhsQ4p078RecZ8U";
            try {
                URL map_image = new URL(map_image_URL);
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection)map_image.openConnection();
                    urlConnection.connect();
                    Log.e(LOGTAG,"the server response for the bmp is : "+urlConnection.getResponseCode());
                    Bitmap bmp = BitmapFactory.decodeStream(map_image.openConnection().getInputStream());
                    ImageView user_location_image = (ImageView)findViewById(R.id.User_Location_Image);
                    user_location_image.setImageBitmap(bmp);
                } catch (IOException e) {
                    Log.e(LOGTAG,"there was a problem getting the input stream from the bmp");
                }
            } catch (MalformedURLException e) {
                Log.e(LOGTAG,"there was a problem converting the map_image_URL string to URL");
            }
            Log.e(LOGTAG,"the map image URL is : "+map_image_URL);
            //todo make a listview and apply the custom adapter to it then get the info in this method and update UI

            List<quakes> earthquakes = QueryUtils.getDataFrom(strings[0],getApplicationContext());
            return earthquakes;

        }

        @Override
        protected void onPostExecute(final List<quakes> data) {
            super.onPostExecute(data);

            MyAdapter itemsAdapter =new MyAdapter((ArrayList<quakes>) data,getApplicationContext());
            nearList.setAdapter(itemsAdapter);
            if(data.isEmpty()){
                ImageView tom = (ImageView)findViewById(R.id.tom_quakesNearMe);
                tom.setVisibility(View.VISIBLE);
                TextView noRecentData = (TextView)findViewById(R.id.text_noRecentData);
                noRecentData.setVisibility(View.VISIBLE);
            }
            nearList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String url = data.get(position).getUrl();
                    Intent intent = new Intent(getApplicationContext(),TappedEarthquake.class);
                    //get the eventid from the json and send to the TappedEarthquake activity
                    intent.putExtra("EVENTID",data.get(position).getId());
                    Log.e(LOGTAG,"evenid for an earthquake was sent to the TappedEarthquake");
                    startActivity(intent);
                    //use this to open in browser
                 /*intent.setData(Uri.parse(url));
                 startActivity(intent);*/

                }
            });
            ProgressBar loadingIndicator = (ProgressBar)findViewById(R.id.loadingIndicator_quakesNearMe);
            loadingIndicator.setVisibility(View.GONE);
        }//postexecute ends

    }//asynctask ends

    @Override
    protected void onPause() {
        super.onPause();
        newRunner.cancel(true);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.e(LOGTAG,"LastLocation method was run");
                            updateURL();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            Log.e(LOGTAG,"the location is : "+latitude+" "+longitude+" newLocationData was requested");

            updateURL();
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }


}//activity ends