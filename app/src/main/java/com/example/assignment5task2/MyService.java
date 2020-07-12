package com.example.assignment5task2;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MyService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    private double latitude, longitude;
    private String url = "http:192.168.1.3:80/A5T2/geoloc.php"; //specify localhost ip
    private HashMap<String,String> geoloc = new HashMap<String,String>();

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        locationManager  =(LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);
        //get device last known location
        //using last known location, get latitude and longitude
        location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        //send geolocation to php
        new postLoc(latitude,longitude).execute();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }

    public class postLoc extends AsyncTask<String, String, String> {

        //add latitude and longitude value to hashmap with lat and long as KEY
        public postLoc(Double lat, Double lon){
            geoloc.put("lat", String.valueOf(lat));
            geoloc.put("lon", String.valueOf(lon));
        }

        public String postData(String url, HashMap<String,String> geoloc){
            URL localHost;
            String response = "";

            OutputStream outputStream;
            HttpURLConnection httpURLConnection;
            try{
                localHost = new URL(url); //specify url
                httpURLConnection = (HttpURLConnection) localHost.openConnection(); //conenct to localhost

                httpURLConnection.setRequestMethod("POST"); //set method to POST
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                outputStream = httpURLConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(passData(geoloc)); //pass the data in geoloc to be appended in passData and pass to php
                writer.flush();
                writer.close();
                outputStream.close();

                //get response from php
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
                Log.d("STATUS",response);

            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            return null;
        }

        private String passData(HashMap<String,String> geoloc) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            //get values in hashmap
            //if its first value do not append with "&" else append with "&" to pass more than 1 value
            for(Map.Entry<String, String> data : geoloc.entrySet()){
                if (first) {
                    first = false;
                }
                else {
                    result.append("&");
                }

                //append the lat and lon into 1 line
                result.append(URLEncoder.encode(data.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(data.getValue(), "UTF-8"));
            }
            Log.d("TEST", result.toString());
            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            postData(url,geoloc);
        }

        @Override
        protected String doInBackground(String... strings) {
            return postData(url,geoloc);
        }
    }
}
