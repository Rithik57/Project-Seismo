package com.example.earthquakedata;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

public class searchBackgroundLoader extends AsyncTaskLoader<List<quakes>>{

    private String url;
    private String keyword;
    private static final String LOGTAG = "searchBackgroundLoader";
    public searchBackgroundLoader(@NonNull Context context,String url,String keyword) {
        super(context);
        //get the url and the keyword from the onCreateLoader and initialize them here
        this.url = url;
        this.keyword = keyword;
        Log.e(LOGTAG,"the searchBackgroundLoader was called with the URL : "+url+"with keyword : "+keyword);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public ArrayList<quakes> loadInBackground() {
        //get the list from the url and keyword using the queryUtils methods
        if(url == null){
            return null;
        }
        Log.e(LOGTAG,"loadinBackground was called for the searchBackgroundLoader");

        List<quakes> earthquakes = QueryUtils.SearchData(url,keyword);
        //return the list to the onCreate method
        return (ArrayList)earthquakes;
    }
}
