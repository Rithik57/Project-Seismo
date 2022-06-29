package com.example.earthquakedata;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

public class backgroundLoader extends AsyncTaskLoader<List<quakes>> {   //the loader will take what the background method returns as the parameter
    //creating a log tag
    public static final String LOG_TAG = backgroundLoader.class.getName();
    //query url
    private String url;
    //constructor will receive the url and the context
    public backgroundLoader(@NonNull Context context,String url) {
        super(context);
        this.url = url;
        Log.e(LOG_TAG,"backgroundLoader's constructor was called in the class");
    }

    @Override
    protected void onStartLoading() {
        Log.e(LOG_TAG,"forceLoad was called in the startLoading method in the backgroundLoader class");
        forceLoad();
    }

    @Nullable
    @Override
    public ArrayList<quakes> loadInBackground() {
        if(url==null){
            return null;
        }
        Log.e(LOG_TAG,"loadInBackground method was called in the backgroundLoader class");
        //gets the data from the server in the background and stores it in a list
        List<quakes> earthquakes = QueryUtils.getDataFrom(url,getContext()); //creating a list to store the earthquakes that are returned by the query utils after parsing
        return (ArrayList<quakes>) earthquakes; //returns the list data to the main thread for UI updating
    }
}
