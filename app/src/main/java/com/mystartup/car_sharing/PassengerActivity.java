package com.mystartup.car_sharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private Location mLocation;
    private Button requestCab;
    boolean isCabRequestPlaced = false;
    private Button logout;
    Timer t;
    private ParseGeoPoint driverLocation;
    private boolean isCabReady;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestCab = findViewById(R.id.cab_request);
        requestCab.setOnClickListener(this);
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null){
                            Intent intent = new Intent(PassengerActivity.this,MainActivity.class);
                            startActivity(intent);}
                        finish();
                    }
                });
            }
        });

        updateRequestCabButton();
        }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateRequestCabButton();
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                     updatePassengerCameraLocation(location);

            }
            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Toast.makeText(PassengerActivity.this,"Passenger Location Updated",Toast.LENGTH_LONG).show();


            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }};
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                    ParseQuery<ParseObject>parseQuery = new ParseQuery<ParseObject>("CabRequest");
                    parseQuery.whereEqualTo("Passenger",ParseUser.getCurrentUser().getUsername());
                    parseQuery.whereEqualTo("Status","Request Accepted");
                    parseQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if(e==null){
                                if(objects.size()>0){
                                    isCabReady = true;
                                    getDriverLocationUpdates();
                                }
                            }
                        }
                    });

                    }},0,5000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);
        }
        else{
            mLocation = getLastKnownLocation();
            updatePassengerCameraLocation(mLocation);
        }
         }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == 1000 && grantResults.length>0 & grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(PassengerActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                mLocation = getLastKnownLocation();
                updatePassengerCameraLocation(mLocation);
            } }
    }

    private void updatePassengerCameraLocation(Location plocation){
            LatLng latLng = new LatLng(plocation.getLatitude(), plocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location is this"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
    }

    private Location getLastKnownLocation() {

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                List<String> providers = mLocationManager.getProviders(true);
                Location bestLocation = null;
                if(ContextCompat.checkSelfPermission(PassengerActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                    for (String provider : providers) {
                        Location l = mLocationManager.getLastKnownLocation(provider);
                        if (l == null) {
                            continue;
                        }
                        if (bestLocation == null
                        || l.getAccuracy() < bestLocation.getAccuracy()) {
                            bestLocation = l;
                        }
                    }
                    if (bestLocation == null) {
                        return null;
                    }
                }
                return bestLocation;
    }

    @Override
    public void onClick(View view) {
        if(!isCabRequestPlaced){
            requestCab.setText("Cancel Cab");  ParseObject parseObject = new ParseObject("CabRequest");
            parseObject.put("Passenger", ParseUser.getCurrentUser().getUsername());
            parseObject.put("Location", new ParseGeoPoint(getLastKnownLocation().getLatitude(),getLastKnownLocation().getLongitude()));
            parseObject.put("Status","Request Placed");
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e!=null){
                        Toast.makeText(PassengerActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });
            isCabRequestPlaced = true;
        }
        else{
            requestCab.setText("Request a Cab");
            ParseQuery<ParseObject>parseQuery = new ParseQuery("CabRequest");
            parseQuery.whereEqualTo("Passenger",ParseUser.getCurrentUser().getUsername());
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(objects.size()>0){
                        for(ParseObject parseObject:objects){
                            parseObject.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e!=null){
                                        isCabRequestPlaced = false;
                                        Toast.makeText(PassengerActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }); } } }
            });
            isCabRequestPlaced = false;


        }
    }

    private void getDriverLocationUpdates(){
        t.cancel();
        try{
        ParseQuery<ParseObject> parseCabRequestQuery = new ParseQuery<ParseObject>("CabRequest");
        parseCabRequestQuery.whereEqualTo("Passenger",ParseUser.getCurrentUser().getUsername());
        parseCabRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size()>0){
                        for(ParseObject parseObject: objects){
                            if(parseObject.get("Status").equals("Request Accepted")){
                                ParseQuery<ParseUser> newParseQuery = ParseUser.getQuery();
                                newParseQuery.whereEqualTo("username",parseObject.get("DriverName"));
                                newParseQuery.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> objects, ParseException e) {
                                        for(ParseObject parseObject1 : objects){

                                            driverLocation = parseObject1.getParseGeoPoint("DriverLocation");
                                            timer = new Timer();
                                            timer.scheduleAtFixedRate(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            updateMap(driverLocation);
                                                            //Toast.makeText(PassengerActivity.this,"Timer is working",Toast.LENGTH_LONG).show();

                                                        }
                                                    });


                                                }
                                            },0,1000);

                                        }
                                    }
                                });

                            } } } } }
        });
    }
    catch (Exception e){

    }
    }

    private void updateMap(ParseGeoPoint parseGeoPoint) {
        mMap.clear();
        LatLng driverLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
        LatLng passengerLatLng = new LatLng(getLastKnownLocation().getLatitude(), getLastKnownLocation().getLongitude());
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Drivers Location"));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(passengerLatLng).title("Driver is here"));

        ArrayList<Marker> arrayMarker = new ArrayList<>();
        arrayMarker.add(passengerMarker);
        arrayMarker.add(driverMarker);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Marker m : arrayMarker) {
            builder.include(m.getPosition());
        }
        LatLngBounds latLngBounds = builder.build();
        try {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 700, 700, 5);
            mMap.animateCamera(cameraUpdate);

        } catch (Exception e) {
            Toast.makeText(PassengerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        //Toast.makeText(PassengerActivity.this, "Location is getting update", Toast.LENGTH_LONG).show();
        if(driverLocation.distanceInKilometersTo(new ParseGeoPoint(getLastKnownLocation().getLatitude(),getLastKnownLocation().getLongitude()))<.2){
           timer.cancel();
        }

    }


    private void updateRequestCabButton(){
        try{
            ParseQuery<ParseObject>parseQuery = new ParseQuery<ParseObject>("CabRequest");
            parseQuery.whereEqualTo("Passenger",ParseUser.getCurrentUser().getUsername());
            parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        if (object != null) {
                            if (object.get("Status") != null) {
                                requestCab.setText("Cancel Cab");
                                isCabRequestPlaced = true;
                            }
                        } else {
                            Toast.makeText(PassengerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });}
        catch (Exception e){ }

    }
}



