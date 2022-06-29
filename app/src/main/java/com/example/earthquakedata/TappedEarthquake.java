package com.example.earthquakedata;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.SimpleTimeZone;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class TappedEarthquake extends AppCompatActivity {

    //creating a log tag
    private static final String LOG_TAG = TappedEarthquake.class.getName();
    String EVENT_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&eventid=";
    String NEWS_REQUEST_URL = "https://content.guardianapis.com/search?q=earthquake+magnitude&api-key=8db2f494-8ffc-4f89-bc00-0d88ead82f9c";
    String IMAGE_REQUEST_URL = "https://api.unsplash.com/search/photos?query=";
    //image request url should be of the type https://api.unsplash.com/search/photos?query=Indonesia&client_id=AXeFo2RuU2Rw-f_Y-_DBVvO0VcEK0p1DV__2P1V93f4


    private String mapURL;
    private String EarthquakeURL;
    private String LocationKeyword;
    //latitude and longitude for the map coordinates
    String lat;
    String lon;
    double mag;

    getQuakeInfo runner;

    ImageView earthImage;


    newsTaskRunner newsRunner;
    String newsUrl; //keyword to search the news articles

    getImageRunner imageRunner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tapped_earthquake);
        //receive the eventId from main and set up a connection to parse the id using JSON
        String eventid = getIntent().getStringExtra("EVENTID");

        LocationKeyword = getIntent().getStringExtra("location");
        LocationKeyword = getKeyword(LocationKeyword);
        Log.e(LOG_TAG,"the location keyword was obtained as : "+LocationKeyword);
        IMAGE_REQUEST_URL = IMAGE_REQUEST_URL+LocationKeyword+"&client_id=AXeFo2RuU2Rw-f_Y-_DBVvO0VcEK0p1DV__2P1V93f4";


        Log.e(LOG_TAG,"the event id was received by the new activity");
        EVENT_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&eventid="+eventid;
        Log.e(LOG_TAG,"the event request URL is : "+EVENT_REQUEST_URL);


        runner = new getQuakeInfo();
        runner.execute(EVENT_REQUEST_URL);

        newsRunner = new newsTaskRunner();
        newsRunner.execute(NEWS_REQUEST_URL);

        imageRunner = new getImageRunner();
        imageRunner.execute(IMAGE_REQUEST_URL);

        earthImage = (ImageView)findViewById(R.id.image_earth_tappedEarthquake);
        YoYo.with(Techniques.FadeIn).duration(1300).playOn(earthImage);
        TextView viewMap = (TextView)findViewById(R.id.text_ViewMap_tappedEarthquake);
        YoYo.with(Techniques.FadeIn).duration(1300).playOn(viewMap);
        Log.e(LOG_TAG,"performed animation");

    }

    //todo make an asynctask to get the image from the json info

    private class getQuakeInfo extends AsyncTask<String, Integer ,quakes>{      //String for url integer for progress last one for type of data

        @Override
        protected quakes doInBackground(String... strings) {
            try{
                URL queryURL = new URL(strings[0]);
                //getting the json response from the server
                String jsonResponse = makeHttpRequest(queryURL);
                Log.e(LOG_TAG,"the jsonresponse was acquired by the background thread");
                //storing the json info in a quakes object
                try{
                    quakes Earthquake = getJsonInfo(jsonResponse); //the JSON info is stored in this object
                    return Earthquake;
                }catch (JSONException e){
                    Log.e(LOG_TAG,"there was a problem parsing the data from the JSON response");
                }
            }catch (MalformedURLException e){
                Log.e(LOG_TAG,"there was a problem converting the string into URL");
            } catch (IOException e) {
                Log.e(LOG_TAG,"there was an IOException from the makeHttpRequest method");
            }
            return null;
        }

        @Override
        protected void onPostExecute(quakes currentQuake) { //this method receives the object from the background thread and updates the UI
            Log.e(LOG_TAG,"the postExecute method was called");
            EarthquakeURL = currentQuake.getUrl();
            updateUI(currentQuake);
        }
    }

    //## this method returns a quake object that has the data given by the json page
    public quakes getJsonInfo(String jsonResponse) throws JSONException{
        Log.e(LOG_TAG,"getJSONinfo method was run");
        quakes object = new quakes();
        JSONObject baseJSON = new JSONObject(jsonResponse);
        JSONObject properties = baseJSON.getJSONObject("properties");
        Log.e(LOG_TAG,"properties was extracted");
        String location = properties.getString("place");
        Log.e(LOG_TAG,"place was extracted");
        double magnitude = properties.getDouble("mag");
        mag = magnitude;    //sending to map
        Log.e(LOG_TAG,"mag was extracted");
        String alert = properties.getString("alert");
        Log.e(LOG_TAG,"alert was extracted");
        String time = properties.getString("time");
        Log.e(LOG_TAG,"time was extracted");
        int tsunamiStatus = properties.getInt("tsunami"); //tsunami status is 0 for false and 1 for true
        Log.e(LOG_TAG,"tsunamiStatus was extracted");
        String url = properties.getString("url");
        Log.e(LOG_TAG,"url was extracted");
        JSONObject products = properties.getJSONObject("products");
        Log.e(LOG_TAG,"products was extracted");
        JSONArray origin = products.getJSONArray("origin");
        Log.e(LOG_TAG,"origin was extracted");
        JSONObject info = origin.getJSONObject(0);
        JSONObject mProperties = info.getJSONObject("properties");
        String latitude = mProperties.getString("latitude");
        String longitude = mProperties.getString("longitude");
        Log.e(LOG_TAG,"location was : "+location+" mag was : "+magnitude+" alert level : "+alert+" time was : "+time+" tsunami status"+tsunamiStatus+" url : "+url+" the latitude was : "+latitude+" the longitude was : "+longitude);
        //storing the info in a quakes object
        object.setLocation(location);
        object.setMag(magnitude);
        object.setAlertLevel(alert);
        object.setTime(time);
        object.setTsunamiStatus(tsunamiStatus);
        object.setUrl(url);
        lat = latitude;
        lon = longitude;
        return object;
    }

    //## this method returns the JSON response corresponding to the request URL given to it
    public static String makeHttpRequest(URL url) throws IOException {
        String JSON_RESPONSE = ""; //placeholder for json response
        HttpURLConnection urlConnection = null; //creating a connection variables
        InputStream inputStream = null; //input stream for reading from the response
        Log.e(LOG_TAG,"the makeHttpRequest method was run");
        try{
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            Log.e(LOG_TAG,"the server response code was : "+urlConnection.getResponseCode());
            Log.e(LOG_TAG,"the server response message was : "+urlConnection.getResponseMessage());
            if(urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                JSON_RESPONSE = readFromStream(inputStream);
            }
        }catch( IOException e){
            Log.e(LOG_TAG,"error in the makeHTTP request method response code from the server is : "+urlConnection.getResponseCode());
        }finally {
            if(urlConnection !=null){
                urlConnection.disconnect();
            }
            if(inputStream !=null){
                inputStream.close();
            }
        }
        return JSON_RESPONSE;
    }

    //## this method reads the inputStream provided to it
    private static String readFromStream(InputStream inputStream) throws IOException{
        StringBuilder output = new StringBuilder();
        if(inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while(line != null){
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        Log.e(LOG_TAG,"the read from stream method was run");
        return output.toString();
    }

    //## this method updates the UI using the object data received in postExecute method
    public void updateUI(quakes object){
        Log.e(LOG_TAG,"the updateUI method was called in the tappedEarthquake class");
        //setting the place in the title
        TextView locationText = (TextView)findViewById(R.id.quakeLocation);
        locationText.setText(object.getLocation());
        //setting the magnitude
        TextView magnitudeText = (TextView)findViewById(R.id.magText);
        double mag = object.getMag();
        magnitudeText.setText(String.valueOf(mag));
        //setting the alertLevel
        TextView alertLevel = (TextView)findViewById(R.id.alertText);
        alertLevel.setText(object.getAlertLevel());
        alertLevel.setTextColor(Color.parseColor("#76ff03"));
        //converting the unix time to a readable format :
        String time = object.getTime();
        long unixTime = Long.parseLong(time);      //converting the time string to unix time
        Date dateObject = new Date(unixTime);       //creating an instance of the Date class and giving it the unix time converts it into a readable format
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss a");
        dateFormatter.setTimeZone(SimpleTimeZone.getTimeZone("GMT+5:30"));
        String readableDate = dateFormatter.format(dateObject);
        TextView timeText = (TextView)findViewById(R.id.timeText);
        timeText.setText(readableDate);
        String TsunamiStatus;
        if(object.getTsunamiStatus() == 1){
            TsunamiStatus = "yes";
        }else{
            TsunamiStatus = "no";
        }
        Log.e(LOG_TAG,"the tsunami status was received as : "+object.getTsunamiStatus());
        TextView tsunamiText = (TextView)findViewById(R.id.tsunamiText);
        tsunamiText.setText(TsunamiStatus);
        if(object.getTsunamiStatus()==1){
            tsunamiText.setTextColor(Color.parseColor("#d50000"));
        }
        mapURL = object.getUrl();
    }

    //opens the earthquake in maps
    public void viewInMap(View view){
        Intent intent = new Intent(getApplicationContext(),quakeMap.class);
        intent.putExtra("latitude",lat);
        intent.putExtra("longitude",lon);
        intent.putExtra("mag",mag);
        Log.e(LOG_TAG,"the lat and lon was sent to the map activity");
        startActivity(intent);
    }

    //redirects to the earthquake info on the USGS website
    public void viewDetails(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(EarthquakeURL));
        startActivity(intent);
    }

    //## destroy the asynctask when the activity pauses
    @Override
    protected void onPause(){
        super.onPause();
        Log.e(LOG_TAG,"the asynctasks were $$destroyed$$ for tappedEarthquake");
        runner.cancel(true);
        newsRunner.cancel(true);
        imageRunner.cancel(true);
    }
    //takes in jsonResponse as input and returns a random news object
    public news getNewsJSONInfo(String jsonResponse) throws JSONException{
        news CurrentNews = new news();
        JSONObject baseJSON = new JSONObject(jsonResponse); Log.e(LOG_TAG,"base JSON was extracted");
        JSONObject response = baseJSON.getJSONObject("response"); Log.e(LOG_TAG,"response was extracted");
        JSONArray results = response.getJSONArray("results"); Log.e(LOG_TAG,"results array was extracted");
        Random rand = new Random();
        int randomInt = rand.nextInt(10);
        JSONObject News1 = results.getJSONObject(randomInt); Log.e(LOG_TAG,"first news object was extracted");
        String webTitle = News1.getString("webTitle");  Log.e(LOG_TAG,"title was extracted");
        String webUrl = News1.getString("webUrl"); Log.e(LOG_TAG,"webUrl was extracted");
        CurrentNews.setTitle(webTitle);
        CurrentNews.setUrl(webUrl);
        newsUrl = webUrl;
        return CurrentNews;
    }

    private class newsTaskRunner extends AsyncTask<String, Integer, news>{

        @Override
        protected news doInBackground(String... strings) {
            URL url = null;
            try {
                url = new URL(strings[0]);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG,"could not conver the news request string to URL");
            }
            String jsonResponse="";
            try {
                jsonResponse = makeHttpRequest(url);
            }catch (IOException e){
                Log.e(LOG_TAG,"there was a problem getting the jsonResponse for the news from the server");
            }
            try {
                news NewsObject = getNewsJSONInfo(jsonResponse);
                return NewsObject;
            } catch (JSONException e) {
                Log.e(LOG_TAG,"there was a problem getting a NewsObject from the json response");
            }
            return null;
        }//background ends

        @Override
        protected void onPostExecute(news NewsObject) {
            super.onPostExecute(NewsObject);
            if(NewsObject == null){
                Log.e(LOG_TAG,"NewsObject was null");
            }else{
                TextView newsTitle = (TextView)findViewById(R.id.news_title);
                newsTitle.setText(NewsObject.getTitle());
            }
        }//Post execute ends

    }//news runner ends

    public void viewNews(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(newsUrl));
        startActivity(intent);
    }

    private String getKeyword(String location){
        String keyword;
        int index = location.indexOf(",");
        if(index>=0) {
            keyword = location.substring(index + 1).trim();
            keyword = keyword.replaceAll("\\s", "+");
            return keyword;
        }else{
            keyword = location.trim().replaceAll("\\s","+");
            return keyword;
        }
    }

    private class getImageRunner extends AsyncTask<String,Integer, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                try {
                    String jsonResponse = makeHttpRequest(url);
                    try {
                        String imageURL = getImageURL(jsonResponse);
                        Log.e(LOG_TAG,"the image url was received as : "+imageURL);
                        //converting the obtained imageURL string to URL and then to bitmap
                        URL bitmapURL = new URL(imageURL);
                        Bitmap bmp = BitmapFactory.decodeStream(bitmapURL.openConnection().getInputStream());
                        return bmp;
                    } catch (JSONException e) {
                        Log.e(LOG_TAG,"there was an error getting the data from the json response");
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG,"there was an error getting a response from the unsplash server");
                }
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG,"the image url string could not be converted to URL");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //todo make an image in the xml and assign the bitmap to it
            ImageView locationImage = (ImageView)findViewById(R.id.locationImage);
            locationImage.setImageBitmap(bitmap);
        }

    }

    private String getImageURL(String jsonResponse) throws JSONException{
        JSONObject baseJSON = new JSONObject(jsonResponse);
        JSONArray results = baseJSON.getJSONArray("results");
        JSONObject imageObject = results.getJSONObject(0);
        JSONObject urls = imageObject.getJSONObject("urls");
        String full = urls.getString("full");
        return full;
    }

}//activity ends