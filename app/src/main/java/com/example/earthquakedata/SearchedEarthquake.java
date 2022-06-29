package com.example.earthquakedata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class SearchedEarthquake extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<quakes>> {

    private static final String LOGTAG = "searched earthquake activity";
    ListView searchList;
    private static  String USGS_REQUEST_URL  = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&limit=5000";
    private static final int SEARCH_LOADER = 2;
    String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searched_earthquake);

        location = getIntent().getStringExtra("location");
        Log.e(LOGTAG,"the location received by the search activity was : "+location);
        TextView currentSearchText = (TextView)findViewById(R.id.currentSearchText);
        currentSearchText.setText(location);

        searchList = (ListView)findViewById(R.id.searchList);

        getSupportLoaderManager().initLoader(SEARCH_LOADER,null, this);

    }
    //## enters a new search location when the image or current location is clicked and launches a new activity
    public void changeLocation(View view){
        //making edittext visible
        final EditText newSearchText = (EditText)findViewById(R.id.newSearchText);
        TextView currentSearchText = (TextView)findViewById(R.id.currentSearchText);
        currentSearchText.setVisibility(View.INVISIBLE);
        newSearchText.setVisibility(View.VISIBLE);
        //forcing input to edittext
        newSearchText.requestFocus();
        newSearchText.setFocusableInTouchMode(true);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(newSearchText, InputMethodManager.SHOW_FORCED);

        //listening when the enter button is pressed
        newSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Log.e(LOGTAG,"new Search location was searched");
                imm.hideSoftInputFromWindow(null,1);
                String newLocation = newSearchText.getText().toString();
                Intent intent = new Intent(getApplicationContext(),SearchedEarthquake.class);
                intent.putExtra("location",newLocation);
                startActivity(intent);
                return true;
            }
        });

    }

    @NonNull
    @Override
    public Loader<List<quakes>> onCreateLoader(int i, Bundle bundle) {
        Log.e(LOGTAG,"onCreateLoader was called for the search");
        return new searchBackgroundLoader(this,USGS_REQUEST_URL,location);
    }

    @Override
    public void onLoadFinished(@NonNull androidx.loader.content.Loader<List<quakes>> loader, List<quakes> data) {
        View loadingIndicator = (View)findViewById(R.id.searchloadingIndicator);
        loadingIndicator.setVisibility(GONE);
        updateUI((ArrayList)data);
    }

    @Override
    public void onLoaderReset(@NonNull androidx.loader.content.Loader<List<quakes>> loader) {

    }

    public void updateUI(final ArrayList<quakes> earthquakes) {
        MyAdapter newAdapter = new MyAdapter(earthquakes, this);
        searchList.setAdapter(newAdapter);
        Log.e(LOGTAG,"the adapter was set to the search results");
        if (earthquakes.isEmpty() == true) {
            Log.e(LOGTAG,"the list was empty");
            searchList.setVisibility(GONE);
            ImageView tom = (ImageView) findViewById(R.id.noSearchFoundTom);
            tom.setVisibility(View.VISIBLE);
            TextView noSearchFound = (TextView) findViewById(R.id.noSearchFoundText);
            noSearchFound.setVisibility(View.VISIBLE);
        }
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String id;
                id = earthquakes.get(position).getId();
                Log.e(LOGTAG,"The event id for the selected earthquake "+id+" was sent to tappedEarthquake");
                Intent intent = new Intent(getApplicationContext(),TappedEarthquake.class);
                intent.putExtra("EVENTID",id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportLoaderManager().destroyLoader(SEARCH_LOADER);

        EditText searchBar = (EditText)findViewById(R.id.newSearchText);
        searchBar.setVisibility(View.INVISIBLE);
        TextView currentSearchText = (TextView)findViewById(R.id.currentSearchText);
        currentSearchText.setVisibility(View.VISIBLE);

    }
}