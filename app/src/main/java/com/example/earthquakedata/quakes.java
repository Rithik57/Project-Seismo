package com.example.earthquakedata;

public class quakes {
    private String Location;
    private double mag;
    private String date;
    private String time;
    private String url;
    private String id;
    private int TsunamiStatus;
    private String alertLevel;
    private int lat;
    private int lon;

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLon() {
        return lon;
    }

    public void setLon(int lon) {
        this.lon = lon;
    }

    public quakes(){

    }
    public quakes(String location,double magnitude,String date,String url,String id){
        Location=location;
        mag=magnitude;
        this.date=date;
        this.time=time;
        this.url=url;
        this.id=id;
    }
    public String getLocation() {
        return Location;
    }
    public void setLocation(String x){
        Location=x;
    }

    public double getMag() {
        return mag;
    }

    public void setMag(double mag) {
        this.mag = mag;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTsunamiStatus() {
        return TsunamiStatus;
    }

    public void setTsunamiStatus(int tsunamiStatus) {
        TsunamiStatus = tsunamiStatus;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }
}
