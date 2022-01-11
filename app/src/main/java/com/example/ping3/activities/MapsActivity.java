package com.example.ping3.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.ping3.models.Player_model;
import com.example.ping3.R;
import com.example.ping3.utils.TimerService;
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
    private FirebaseAnalytics mFirebaseAnalytics;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    FirebaseAuth fAuth;
    Player_model player = new Player_model();
    String room_id;
    TimerReceiver timerReceiver = null;
    boolean isMouse = false;
    double lastX = 0;
    double lastY = 0;
    int timeSetted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        fAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        room_id = intent.getStringExtra("room_id");
        timeSetted = intent.getIntExtra("time",60);
        if (intent.getStringExtra("Mouse") != null) {
            if (intent.getStringExtra("Mouse").equals("yes")) {
                isMouse = true;
            }
        }
        if (intent.getDoubleExtra("lastX", 0) != 0 &&
                intent.getDoubleExtra("lastY", 0) != 0) {
            lastX = intent.getDoubleExtra("lastX", 0);
            lastY = intent.getDoubleExtra("lastY", 0);
        }
        initTimeReceiver();

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        if (lastX != 0 && lastY != 0){
            LatLng pin = new LatLng(lastX, lastY);
            mMap.addMarker(new MarkerOptions().position(pin));
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }




    public void getOthersPosition(){
        mMap.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final double[] x = new double[1];
        final double[] y = new double[1];
        float zoomLevel = 15.0f;

        db.collection("player").whereEqualTo("Room_id",room_id).whereNotEqualTo("UID",fAuth.getUid())
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
                            }
                        } else {
                            Toast.makeText(MapsActivity.this,"Error getting documents."+task.getException(),Toast.LENGTH_LONG).show();
                            Log.w("TAG111", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getMousePosition(){
        mMap.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final double[] x = new double[1];
        final double[] y = new double[1];
        float zoomLevel = 15.0f;
        db.collection("player").whereEqualTo("Room_id",room_id).whereNotEqualTo("isMouse",false)
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
                            }
                        } else {
                            Toast.makeText(MapsActivity.this,"Error getting documents."+task.getException(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
                    } else{
                        Toast.makeText(MapsActivity.this,"Current location is null",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (SecurityException e){
            Toast.makeText(this, "SecurityException :"+ e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void UpdatePosition(double x,double y){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update information of position
        db.collection("player").document(player.getEmail()).update("X",x);
        db.collection("player").document(player.getEmail()).update("Y",y);

    }

    //TimerReceiver
    public class TimerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals("com.demo.timer")) {
                int time = intent.getIntExtra("time", 0);

                if((timeSetted*60 - time) == 0){
                    stopTimer();
                    Toast.makeText(getApplicationContext(),"The room is over",Toast.LENGTH_SHORT).show();
                    Intent home = new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);;
                    startActivity(home);
                }

                if(time % 5 == 0 && isMouse){
                    getOthersPosition();
                }
                if(time % 30 == 0 && !isMouse ){
                    getMousePosition();
                }

            }
        }
    }

    public void initTimeReceiver(){
        timerReceiver = new TimerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.demo.timer");
        registerReceiver(timerReceiver, filter);
    }

    public void stopTimer(){
        //Stop service
        stopService(new Intent(MapsActivity.this, TimerService.class));
        unregisterReceiver(timerReceiver);
        timerReceiver=null;
    }

}