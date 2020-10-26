package com.example.locationsensor;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.lang.String;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {
    //
    private LocationManager locationManager;
    private double longitude;
    private double latitude;
    private double altitude;
    private Location location;
    private SensorManager sensorManager;
    private Sensor sensorLight;
    private int su;
    private float[] lightValues = new float[20];
    private int i = 0;
    float lightSensorValue;
    //origin location
    private double oLongitude;
    private double oLatitude;
    private String s = new String();
    private String address = new String();
    //TextView
    private TextView vLocation;
    private TextView vLongitude;
    private TextView vLatitude;
    private TextView vAltitude;
    private TextView vLightSensor;
    private TextView vHistory;
    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find textview ID
        vLocation = findViewById(R.id.location);
        vLongitude = findViewById(R.id.longitude);
        vLatitude = findViewById(R.id.latitude);
        vAltitude = findViewById(R.id.altitude);
        vLightSensor = findViewById(R.id.lightSensor);
        vHistory = findViewById(R.id.history);
        //Location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }


        location = locationManager.getLastKnownLocation(GPS_PROVIDER);
        oLatitude = location.getLatitude();
        oLongitude = location.getLongitude();

        //func location
        //
        //Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (sensorLight == null) {
            vLightSensor.setText("No sensor");
        }


        onLocationChanged(location);
        //locationFunc(location);
        //func sensor
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        altitude = location.getAltitude();
        float[] distance = new float[1];

        //"oLongtitude: "+ oLongitude + "   "oLongtitude: "+ oLatitude + "  + "\nDistanceToOrigin:" + distance[0]
        vLongitude.setText("Longitude: " + longitude +"° ");
        vLatitude.setText("Latitude: " + latitude + "° ");
        vAltitude.setText("Altitude: " + altitude + "m\n");

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            address = addresses.get(0).getAddressLine(0);
            vLocation.setText("\nAddress: " + address + " ");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error:" + e, Toast.LENGTH_SHORT).show();
        }

        Location.distanceBetween(oLatitude, oLongitude, latitude, longitude, distance);
        // distance[0] is now the distance between these lat/lons in meters
        if (distance[0] > 20.0) {
            oLatitude = latitude;
            oLongitude = longitude;
            distance[0] = 0;
            i=0;
            su=0;
        }

        if(i<20){
            lightValues[i] = lightSensorValue;
            su += lightSensorValue;
            s = "\n_____________________________\n" + address + "\nLongitude: " + longitude + "° \nLatitude: " + latitude + "° \n" + "Altitude: " + altitude + "m \nAvgLightValue = " + su/(i+1);
            vHistory.append(s);
            i++;}else{i=0; su = 0;}
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //function getting current address;
    private void locationFunc(Location location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            vLocation.setText("\nAddress: " + address + " ");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error:" + e, Toast.LENGTH_SHORT).show();
        }
    }

    //Sensor
    @Override
    protected void onStart() {
        super.onStart();

        if (sensorLight != null) {
            sensorManager.registerListener(this, sensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (location != null) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 2, this);
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        lightSensorValue = event.values[0];

        switch (sensorType) {
            case Sensor.TYPE_LIGHT:
                float avg = i ,sum = 0;
                vLightSensor.setText("Current light Sensor: " + lightSensorValue);
                /*for(int j=0; j<i;j++){
                    sum += lightValues[j];
                    vLightSensor.append(" " + lightValues[j]);
                }
                vLightSensor.append("\nSum: " + sum +" Avg: " + sum/avg + "\n");*/
                break;
            default:
        }
        vLightSensor.append("\n\nHistory: ");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
