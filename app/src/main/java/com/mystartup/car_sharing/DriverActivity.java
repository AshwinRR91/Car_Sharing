package com.mystartup.car_sharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DriverActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private Button nearByDriveRequests;
    private ListView mListView;
    private ArrayList<String> mArrayList;
    private ArrayAdapter mArrayAdapter;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location driverOfLocation;
    private ArrayList<String> passengerName;
    private ArrayList<Double>pLatitude;
    private ArrayList<Double>pLongitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        nearByDriveRequests = findViewById(R.id.nearby_drive_request);
        mListView = findViewById(R.id.list_view);
        mArrayList = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter(DriverActivity.this, android.R.layout.simple_list_item_1, mArrayList);
        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(this);
        nearByDriveRequests.setOnClickListener(this);
        passengerName = new ArrayList<>();
        pLatitude = new ArrayList<>();
        pLongitude = new ArrayList<>();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateDriverLocation();
            }
            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.log_out){
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    Intent intent = new Intent(DriverActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,5,mLocationListener);
            Location location = getLastKnownLocation();
            updateListView(location);
        }
    }

    private Location getLastKnownLocation() {

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        if(ContextCompat.checkSelfPermission(DriverActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
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

    private void updateListView(Location driverLocation){
        if(driverLocation!=null) {
            if(mArrayList.size()>0||passengerName.size()>0||pLongitude.size()>0||pLatitude.size()>0) {
                mArrayList.clear();
                passengerName.clear();
                pLongitude.clear();
                pLatitude.clear();
            }
            driverOfLocation = driverLocation;
            final ParseGeoPoint parseGeoPoint = new ParseGeoPoint(driverOfLocation.getLatitude(), driverOfLocation.getLongitude());
            try {
                ParseQuery<ParseObject> mParseQuery = new ParseQuery<ParseObject>("CabRequest");
                mParseQuery.whereNear("Location", parseGeoPoint);
                mParseQuery.whereNotEqualTo("Status","Request Accepted");
                mParseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            if (objects.size() > 0) {
                                for (ParseObject parseObject : objects) {
                                    float d = (float) parseGeoPoint.distanceInKilometersTo(parseObject.getParseGeoPoint("Location"));
                                    d = Math.round(d*10)/10;
                                    mArrayList.add(parseObject.get("Passenger") + "is at a distance of " + d + " kilometers");
                                    passengerName.add(parseObject.get("Passenger").toString());
                                    pLatitude.add(parseObject.getParseGeoPoint("Location").getLatitude());
                                    pLongitude.add(parseObject.getParseGeoPoint("Location").getLongitude());
                                }
                                mArrayAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(DriverActivity.this, "No Nearby Requests", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(DriverActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (Exception e) {

            }
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == 1000 && grantResults.length>0 & grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(DriverActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            Location location = getLastKnownLocation();
            updateListView(location);
            } }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
     Location location = getLastKnownLocation();
     mLocationManager =(LocationManager) getSystemService(LOCATION_SERVICE);
    Intent intent = new Intent(DriverActivity.this,ViewLocationsMap.class);
    intent.putExtra("dLatitude",location.getLatitude());
    intent.putExtra("dLongitude",location.getLongitude());
    intent.putExtra("passenger_name",passengerName.get(position));
    intent.putExtra("pLongitude",pLongitude.get(position));
    intent.putExtra("pLatitude",pLatitude.get(position));
    startActivity(intent);

    }

    private void updateDriverLocation(){
        Location knownLocation = getLastKnownLocation();
        ParseUser parseUser = ParseUser.getCurrentUser();
        parseUser.put("DriverLocation",new ParseGeoPoint(knownLocation.getLatitude(),knownLocation.getLongitude()));
        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
            }
}