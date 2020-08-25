package com.mystartup.car_sharing;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationsMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button acceptRequest;
    private double passengerLatitude,passengerLongitude,driverLatitude,driverLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_locations_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        acceptRequest = findViewById(R.id.acceptRequest);
        acceptRequest.setText(getIntent().getStringExtra("passenger_name"));
        passengerLatitude = getIntent().getDoubleExtra("pLatitude",0);
        passengerLongitude = getIntent().getDoubleExtra("pLongitude",0);
        driverLongitude =getIntent().getDoubleExtra("dLongitude",0);
        driverLatitude = getIntent().getDoubleExtra("dLatitude",0);
        acceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("CabRequest");
                    parseQuery.whereEqualTo("Passenger",getIntent().getStringExtra("passenger_name"));
                    parseQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if(e==null){
                                if(objects.size()>0){
                                    for(ParseObject parseObject:objects){
                                        parseObject.remove("Status");
                                        parseObject.put("Status","Request Accepted");
                                        parseObject.put("DriverName",ParseUser.getCurrentUser().getUsername());
                                        parseObject.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if(e!=null){
                                                    Toast.makeText(ViewLocationsMap.this,e.getMessage(),Toast.LENGTH_LONG).show();
                                                    Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&origin="+19.019537+","+73.016171+"&destination="+19.021606+","+73.015077));
                                                    startActivity(googleIntent);

                                                }
                                            }
                                        });
                                    }
                                }
                                else{Toast.makeText(ViewLocationsMap.this,"object size <0",Toast.LENGTH_LONG).show();

                                }
                                 }else {
                                Toast.makeText(ViewLocationsMap.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                }

                catch (Exception e){
                    Log.i("Error code",e.getMessage());

                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng passengerLatLng = new LatLng(passengerLatitude,passengerLongitude);
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(passengerLatLng).title("Passenger is Here"));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(passengerLatLng));

        LatLng driverLatLng = new LatLng(driverLatitude,driverLongitude);
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver is Here"));

        ArrayList<Marker> mArrayMarker = new ArrayList<>();
        mArrayMarker.add(passengerMarker);
        mArrayMarker.add(driverMarker);

        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
//19.021092, 73.028764
        for(Marker m:mArrayMarker){
            latLngBounds.include(m.getPosition());
        }

        LatLngBounds lngBounds = latLngBounds.build();
        try {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(lngBounds, 100,100,5);
            mMap.animateCamera(cameraUpdate);

        }
        catch(Exception e){
            Toast.makeText(ViewLocationsMap.this,e.getMessage(),Toast.LENGTH_LONG).show();}
        }
}