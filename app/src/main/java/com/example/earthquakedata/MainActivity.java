package com.example.earthquakedata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;

import android.app.ActionBar;
import android.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<quakes>>, SwipeRefreshLayout.OnRefreshListener {
    //request url for the server
    private static  String USGS_REQUEST_URL  = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake";
    //creating an identifier for the background loader
    private static final int QUAKE_LOADER = 1;  //loader for the home screen
    //creating a log tag
    private static final String LOG_TAG = MainActivity.class.getName();
    SwipeRefreshLayout swipeRefresh; //swipe to refresh container
    LoaderManager loaderManager;
    List<quakes> HomePageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loaderManager = getSupportLoaderManager();
        getSupportLoaderManager().initLoader(QUAKE_LOADER,null,this);
        Log.e(LOG_TAG,"onCreate was run with the support loader");

        ImageView earthIconHome = (ImageView)findViewById(R.id.earthIconHome);
        Date currentTime = Calendar.getInstance().getTime();
        Log.e(LOG_TAG,"the current hour is : "+currentTime.getHours());
        if(currentTime.getHours()>=19 || currentTime.getHours()<=5){
            earthIconHome.setImageResource(R.drawable.earth_night_icon_xxhdpi);
        }else{
            earthIconHome.setImageResource(R.drawable.earth_day_icon_xxhdpi);
        }

        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this);


    }


    //## Loader creates a background thread to get the data from the server and send it to the loadfinished method to update the list
    @NonNull
    @Override
    public Loader<List<quakes>> onCreateLoader(int id, @Nullable Bundle args) { //this method finds the loader to be implemented | return type is the object to be returned by the background task
        Log.e(LOG_TAG,"onCreateLoader called the backgroundLoader class");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPreferences.getString(getString(R.string.settings_min_magnitude_key),getString(R.string.settings_min_magnitude_default));   //string for getting the min magnitude from the key of preference
        String orderBy = sharedPreferences.getString(getString(R.string.settings_order_by_key),getString(R.string.settings_order_by_default));
        USGS_REQUEST_URL = USGS_REQUEST_URL+"&minmag="+minMagnitude;
        USGS_REQUEST_URL = USGS_REQUEST_URL+"&orderby="+orderBy;
        return new backgroundLoader(this,USGS_REQUEST_URL);     //return the loader
    }

    //## this method is called when the earthquake list is obtained from the server and sent to the main thread
    @Override
    public void onLoadFinished(@NonNull Loader<List<quakes>> loader, List<quakes> data) {
        Log.e(LOG_TAG,"loadFinished was called in the main activity and the UI was updated");
        View loadingIndicator = findViewById(R.id.loadingIndicator);
        loadingIndicator.setVisibility(View.GONE);
        HomePageList =data;
        updateUI(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<quakes>> loader) {
        Log.e(LOG_TAG,"Loader was reset in the Main Activity");
    }


    //## refreshes the activity on swiping up
    @Override
    public void onRefresh() {
        Log.e(LOG_TAG,"the home page was refreshed via the onRefresh method");
        loaderManager.restartLoader(QUAKE_LOADER,null,this);
        swipeRefresh.setRefreshing(false);
    }

    private class getDataFromServer extends AsyncTask<String,Void,ArrayList<quakes>>{

        @Override
        protected ArrayList<quakes> doInBackground(String... strings) {
            final ArrayList<quakes> earthquakes = QueryUtils.getDataFrom(strings[0],getApplicationContext());
            return earthquakes;
        }

        @Override
        protected void onPostExecute(ArrayList<quakes> earthquakes) {
            updateUI(earthquakes);
        }
    }


    //## updates the UI with the list form the loader
    public void updateUI(final List<quakes> earthquakes){
         ListView mainList = (ListView) findViewById(R.id.mainList);
         MyAdapter itemsAdapter =new MyAdapter((ArrayList<quakes>) earthquakes,this);
         mainList.setAdapter(itemsAdapter);
        if(earthquakes.isEmpty()){
            Intent intent = new Intent(this,emptyList.class);
            startActivity(intent);
        }
         mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                 String url = earthquakes.get(position).getUrl();
                 Intent intent = new Intent(getApplicationContext(),TappedEarthquake.class);
                 //get the eventid from the json and send to the TappedEarthquake activity
                 intent.putExtra("EVENTID",earthquakes.get(position).getId());
                 intent.putExtra("location",earthquakes.get(position).getLocation());
                 Log.e(LOG_TAG,"evenid for an earthquake was sent to the TappedEarthquake");
                 startActivity(intent);
                 //use this to open in browser
                 /*intent.setData(Uri.parse(url));
                 startActivity(intent);*/

             }
         });
     }


     // create options in the app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(LOG_TAG,"the onCreateOptions method was run in main");
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.e(LOG_TAG,"the onOptionsItemSelected was run in main");
        int id = item.getItemId();

        if(id == R.id.action_settings){
            Log.e(LOG_TAG,"the settings icon was clicked");
            Intent intent = new Intent(this, quakeSettings.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.search_icon){
            Log.e(LOG_TAG,"the search icon was clicked");
            final EditText searchBar = (EditText)findViewById(R.id.homeSearchBar);
            searchBar.setVisibility(View.VISIBLE);

            searchBar.requestFocus();
            searchBar.setFocusableInTouchMode(true);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchBar, InputMethodManager.SHOW_FORCED);

            searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    Log.e(LOG_TAG,"the search bar was activated");
                    Intent intent = new Intent(getApplicationContext(),SearchedEarthquake.class);
                    intent.putExtra("location",searchBar.getText().toString());
                    Log.e(LOG_TAG,"main activity sent the location "+searchBar.getText()+" to the searchedEarthquake activity");
                    startActivity(intent);
                    return true;
                }
            });
        }

        if(id == R.id.nearMe_activity){
            Log.e(LOG_TAG,"the near me icon was clicked ");
            Intent intent = new Intent(this,quakesNearMe.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EditText searchBar = (EditText)findViewById(R.id.homeSearchBar);
        searchBar.setVisibility(View.INVISIBLE);
    }

}