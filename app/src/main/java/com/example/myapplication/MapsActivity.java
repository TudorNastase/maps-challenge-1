package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myapplication.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LocationManager locationManager;
    private LocationListener locListener;
    private List<Location> locations = new ArrayList<>();
    private final long MIN_TIME=20;//20 ms
    private final long MIN_DIST=1; // probably 1 meter
    private LatLng latLng;
    private boolean displayFlag;
    private final int sensorDelay = 50;
    private boolean marker=false;



    private final Runnable sensorDelayer = new Runnable() {
        @Override
        public void run() {
            while(true){
                displayFlag = true;
                try {
                    Thread.sleep(sensorDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        new Thread(sensorDelayer).start();


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        displayFlag=false;

        locListener=new LocationListener() {
            @Override

                public void onLocationChanged (@NonNull Location location){
                if (marker) {
                    try {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Pothole"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        marker = false;


                    }
                marker=false;
                }
            }

            @Override
            public void onStatusChanged(String s,int i, Bundle bundle){

            }
            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };
        locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locListener);
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private float[] readings= new float[1000000];
    private int k=0;

    private int sensitivity=15;
    int l=0;
    int r=sensitivity;
    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer && displayFlag){
            //TextView textView = findViewById(R.id.textViewTest);

            float avg = 0;

            readings[k] = event.values[2];
            k++;
            if(k>=sensitivity){
                avg = average(l,r);
                l++;
                r++;
                if (readings[k-1]>(Math.abs(avg)+2)){
                    marker=true;
                    //System.out.println(readings[k]);
                }
            }
        }
        if (r>999900){
            readings=  new float[1000000];
            l=0;
            r=sensitivity;
        }
        displayFlag = false;


    }

    private float average(int l, int r){
        float sum = 0;
        int ct = r - l;
        for (int i = l; i < r; i++){
            sum += readings[i];
        }
        //System.out.println(sum/ct);
        return sum/ct;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}