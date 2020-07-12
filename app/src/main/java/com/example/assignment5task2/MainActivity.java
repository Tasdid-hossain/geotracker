package com.example.assignment5task2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button start, stop;
    private EditText minutes;
    private int LOCATION_PERMISSION_CODE = 1;
    Handler handler;
    Boolean track = false;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = (Button)findViewById(R.id.start);
        stop = (Button)findViewById(R.id.stop);
        minutes = (EditText)findViewById(R.id.minutes);

        //Request for permission to access GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);

        //disable both buttons
        start.setEnabled(false);
        stop.setEnabled(false);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if interval not specified, Toast warning
                if(minutes.getText().toString().equals("") || minutes.getText().toString().equals("0")){
                    Toast.makeText(getApplicationContext(),"Please Enter Interval Time", Toast.LENGTH_SHORT).show();
                } else {
                    start.setEnabled(false); //disable start button
                    stop.setEnabled(true); //enable stop button
                    track = true; //set track to true
                    handler = new Handler(); //initialize new handler (timer)
                    double mins = Double.parseDouble(minutes.getText().toString()); //get interval user specified
                    final int delay = (int)mins * 60000; //convert to milliseconds
                    intent = new Intent(MainActivity.this, MyService.class);
                    startService(intent); //start myService
                    //start myService(track latitude&longitude) after every interval specified
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startService(intent);
                            //only start the myService if track is true
                            if(track) {
                                handler.postDelayed(this, delay);
                            }
                        }
                    }, delay);
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop.setEnabled(false); //disable stop button
                start.setEnabled(true); //enable start button
                track = false; //set track to false
                handler.removeMessages(0); //clear timer
                stopService(intent); //stop myService(tracking)
            }
        });
    }

    //check if GPS permission granted
    //if granted, enable start button
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                start.setEnabled(true);
            }
        }
    }
}
