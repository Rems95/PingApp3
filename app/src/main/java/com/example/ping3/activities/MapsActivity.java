package com.example.ping3.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.ping3.models.Player_model;
import com.example.ping3.R;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Location mLastLocation;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    FirebaseAuth fAuth;
    Player_model player = new Player_model();
    String room_id;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent =getIntent();
        room_id = intent.getStringExtra("room_id");
        addPlayer();
        getDeviceLocation();

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

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        getDeviceLocation();
        getOthersPosition();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        getDeviceLocation();

    }

    private void getDeviceLocation(){
        float zoomLevel = 15.0f;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Location currentLocation = (Location)task.getResult();
                        //Toast.makeText(MapsActivity.this,"Current location is"+currentLocation.getLongitude()+","+currentLocation.getLatitude(),Toast.LENGTH_SHORT).show();
                        UpdatePosition(currentLocation.getLatitude(),currentLocation.getLongitude());
                        LatLng pin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pin, zoomLevel));
                    } else{
                        Toast.makeText(MapsActivity.this,"Current location is null",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (SecurityException e){
            Toast.makeText(this, "SecurityException :"+ e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void addPlayer(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        // Create a new user with a first and last name
        player.setPlayer_id(fAuth.getUid());
        player.setEmail(fAuth.getCurrentUser().getEmail());



        player.setPseudo("New Player");

        Map<String, Object> user = new HashMap<>();
        user.put("UID", player.getPlayer_id());
        user.put("Email", player.getEmail());
        user.put("Pseudo", player.getPseudo());
        user.put("Room_id",room_id);

        db.collection("player").document(player.getEmail()).set(user);


    }

    public void UpdatePosition(double x,double y){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update information of position
        db.collection("player").document(player.getEmail()).update("X",x);
        db.collection("player").document(player.getEmail()).update("Y",y);

    }

    public void getOthersPosition(){
        mMap.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final double[] x = new double[1];
        final double[] y = new double[1];
        float zoomLevel = 15.0f;

        db.collection("player").whereEqualTo("Room_id",room_id).whereNotEqualTo("UID",player.getPlayer_id())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(MapsActivity.this,"OK",Toast.LENGTH_SHORT).show();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                x[0] = Double.parseDouble(document.getData().get("X").toString());
                                y[0] = Double.parseDouble(document.getData().get("Y").toString());
                                LatLng pin = new LatLng(x[0], y[0]);
                                mMap.addMarker(new MarkerOptions().position(pin));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pin, zoomLevel));
                                //Toast.makeText(MapsActivity.this,"x"+document.getData().get("X"),Toast.LENGTH_SHORT).show();

                            }
                        } else {
                            Toast.makeText(MapsActivity.this,"Error getting documents."+task.getException(),Toast.LENGTH_LONG).show();
                            Log.w("TAG111", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}