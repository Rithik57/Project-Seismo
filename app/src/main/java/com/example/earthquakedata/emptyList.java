package com.example.earthquakedata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class emptyList extends AppCompatActivity {
    private String LOG_TAG = "emptyList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_list);
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        TextView response = (TextView)findViewById(R.id.responseToUser);
        if(isConnected==false){
            response.setText("Connection Error");
        }
    }

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
            Intent intent = new Intent(getApplicationContext(), quakeSettings.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}