package com.example.earthquakedata;

import android.content.Context;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;

//error code 1 is for invalid URL and will report as MalformedException
//this class will be used to extract the data from the server and return an arrayList object to fill the original arrayList in the main method
public final class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getSimpleName(); //creating a log tag

    public static ArrayList<quakes> getDataFrom(String serverLink, Context context){
        //creating the URL from the server link
        URL url = null;
        try{
            url = new URL(serverLink);
        }catch (MalformedURLException e){   //in case the url is invalid this exception will be thrown
            Toast.makeText(context, "Server URL is invalid Error Code : 1", Toast.LENGTH_SHORT).show();
        }
        //create a placeholder jsonResponse
        String jsonResponse = "";
        //get the json Response from the makeHTTPRequest method
        try{
            jsonResponse = makeHttpRequest(url);
        }catch(IOException e){
            Log.e(LOG_TAG,"making the Http request threw an exeption in the QueryUtils class");
        }

        ArrayList<quakes> earthquakes = extractEarthquakes(jsonResponse);
        return earthquakes;
    }

    //##this method is used to get the quakes with a particular string for searching
    public static ArrayList<quakes> SearchData(String serverLink,String keyword){
        //convert the string to URL
        URL url = null;
        try{
            url = new URL(serverLink);
        }catch(MalformedURLException e){
            Log.e(LOG_TAG,"the url received by the searchData method in queryUtils called by the searchBackgroundLoader was null");
        }
        //get the JSON response from the URL
        String JSONresponse = "";
        try{
            JSONresponse = makeHttpRequest(url);
        }catch(IOException e){
            Log.e(LOG_TAG,"there was a problem while getting the json response from the makeHttpRequest method");
        }
        //use the json response to get the required quakes from the list
        ArrayList<quakes> earthquakes = SearchEarthquakes(JSONresponse,keyword);
        //return the list to the background loader
        return earthquakes;
    }

    public static String makeHttpRequest(URL url) throws IOException {
        //create a placeholder json
        String JSONRESPONSE = "";
        //open the HTTP connection
        HttpURLConnection urlConnection =null;  //connection for the url
        InputStream inputStream = null;         //for reading the data from the server page
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //if the server gives the proper status code then proceed with the request
            if(urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                JSONRESPONSE = readFromStream(inputStream);
            }
        }catch(IOException e ){
            Log.e(LOG_TAG,"error getting the json response in the http request method",e);
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(inputStream != null){
                inputStream.close();
            }
            }
        return JSONRESPONSE;
    }

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
        Log.e(LOG_TAG,"the readFromStream method was run");
        return output.toString();
    }

    //this extract method will return the arrayList object to the mainActivity when called
    public static ArrayList<quakes> extractEarthquakes(String jsonResponse) {

        // Create an empty ArrayList that we can start adding earthquakes to
        ArrayList<quakes> earthquakes = new ArrayList<>();      //temp arrayList to return

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            //getting the data using JSON parsing
            JSONObject baseJSONresponse = new JSONObject(jsonResponse);         //creating the root JSONObject using the response from the server
            JSONArray earthquakeArray = baseJSONresponse.getJSONArray("features");      //for finding an array
            for(int i=0;i<earthquakeArray.length();i++){                                    //for iterating through each object of the array
                JSONObject currentQuake = earthquakeArray.getJSONObject(i);             //getting each object in the array
                String id = currentQuake.getString("id");
                JSONObject quakeProperties = currentQuake.getJSONObject("properties");     //getting the properties object
                double magnitude = quakeProperties.getDouble("mag");                //getting strings from the properties object
                String location = quakeProperties.getString("place");
                String time = quakeProperties.getString("time");
                String url = quakeProperties.getString("url");
                int tsunamiStatus = quakeProperties.getInt("tsunami");
                //converting the unix time to a readable format :
                long unixTime = Long.parseLong(time);      //converting the time string to unix time
                Date dateObject = new Date(unixTime);       //creating an instance of the Date class and giving it the unix time converts it into a readable format
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss a");
                dateFormatter.setTimeZone(SimpleTimeZone.getTimeZone("GMT+5:30"));
                String readableDate = dateFormatter.format(dateObject);
                //converting the magnitude into 1 decimal :
                DecimalFormat decimalMag = new DecimalFormat("0.0");
                decimalMag.format(magnitude);

                //sending the data to the arrayList :
                quakes newEarthquake = new quakes(location,magnitude,readableDate,url,id);         //creating an instance of the defining class and feeding it the data from the server
                newEarthquake.setTsunamiStatus(tsunamiStatus);
                earthquakes.add(newEarthquake);         //adding the object to the temporary array list
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // returning the temporary arrayList to the mainActivity
        return earthquakes;
    }

    //this method will extract the quakes corresponding to the seach keyword given
    public static ArrayList<quakes> SearchEarthquakes(String jsonResponse,String keyword){
        Log.e(LOG_TAG,"the SearchEarthquakes method was called in queryutils with the keyword "+keyword);
        //create a placeholder arraylist to store the quakes
        ArrayList<quakes> earthquakes = new ArrayList<>();
        //parse the jsonResponse and get the required quakes
        try{
            JSONObject baseJSONresponse = new JSONObject(jsonResponse);         //creating the root JSONObject using the response from the server
            JSONArray earthquakeArray = baseJSONresponse.getJSONArray("features");      //for finding an array
            for(int i=0;i<5000;i++) {//for iterating through each object of the array
                Log.e(LOG_TAG,"running for "+(i+1)+"th object");
                JSONObject currentQuake = earthquakeArray.getJSONObject(i);             //getting each object in the array
                String id = currentQuake.getString("id");
                JSONObject quakeProperties = currentQuake.getJSONObject("properties");     //getting the properties object
                double magnitude = quakeProperties.getDouble("mag");                //getting strings from the properties object
                String location = quakeProperties.getString("place");
                String time = quakeProperties.getString("time");
                String url = quakeProperties.getString("url");

                //converting the unix time to a readable format :
                long unixTime = Long.parseLong(time);      //converting the time string to unix time
                Date dateObject = new Date(unixTime);       //creating an instance of the Date class and giving it the unix time converts it into a readable format
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss a");
                dateFormatter.setTimeZone(SimpleTimeZone.getTimeZone("GMT+5:30"));
                String readableDate = dateFormatter.format(dateObject);
                //converting the magnitude into 1 decimal :
                DecimalFormat decimalMag = new DecimalFormat("0.0");
                decimalMag.format(magnitude);

                //sending the data to the arrayList :
                quakes newEarthquake = new quakes(location, magnitude, readableDate, url, id);
                int index = newEarthquake.getLocation().toLowerCase().indexOf(keyword.toLowerCase());
                if(index>=0){
                  earthquakes.add(newEarthquake);
                  Log.e(LOG_TAG,"keyword found earthquake was added to list ");
                }
            }
            }catch (JSONException e){
            Log.e(LOG_TAG,"there was a problem while parsing the json response in the search earthquakes method");
        }
            return earthquakes;
    }
}
