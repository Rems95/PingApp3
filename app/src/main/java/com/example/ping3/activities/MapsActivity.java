package com.example.ping3.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.ping3.R;
import com.example.ping3.models.Player_model;
import com.example.ping3.models.gameroom_model;
import com.example.ping3.utils.TimerService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Toolbar myToolbar;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MapActivity";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    FirebaseAuth fAuth;
    Player_model player = new Player_model();
    String room_id,id,pseudo;
    TimerReceiver timerReceiver = null;
    String Mouse = null;
    boolean isMouse = false;
    int timeSetted;
    FloatingActionButton floatingActionButton;
    private BottomSheetBehavior mBottomSheetBehavior1;
    LinearLayout tapactionlayout;
    View white_forground_view;
    View bottomSheet;
    ImageView chat;
    TextView timer_tv;
    int endHideTime = 0;
    boolean startHide = false;
    DatabaseReference myRef_initial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        fAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_maps);
        initView();
        initLayout();
        getExtra();
        getTime();
        initTimeReceiver();
        addPlayer();
        getDeviceLocation();
        displayActionBar();
        //startTimer();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
                timer_tv.setText(timeCalculate(timeSetted -time));
                if (time != 0){
                    if((timeSetted - time) == 0){
                        stopTimer();
                        //unregisterReceiver(timerReceiver);
                        timerReceiver=null;
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
                    if(time % 3 == 0){
                        if (isMouse){
                            if(startHide){
                                if (endHideTime == 0){
                                    endHideTime = time +10;
                                    UpdatePosition(0,0);
                                }
                                else if(endHideTime > time){
                                    UpdatePosition(0,0);
                                }
                                else{
                                    startHide =false;
                                    getDeviceLocation();
                                }
                            }
                            else{
                                getDeviceLocation();
                            }
                        }
                        else{
                            getDeviceLocation();
                        }
                    }

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

    public void startTimer(){
        if(timerReceiver==null){
            timerReceiver = new TimerReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.demo.timer");
            registerReceiver(timerReceiver, filter);
        }
        //start timer service
        startService(new Intent(getApplicationContext(), TimerService.class));
    }

    public void stopTimer(){
        //Stop service
        stopService(new Intent(MapsActivity.this, TimerService.class));
        unregisterReceiver(timerReceiver);
        timerReceiver=null;
    }

    public void getExtra(){
        Bundle extra = getIntent().getExtras();
        room_id = extra.getString("room_id");
        id = extra.getString("id");
        pseudo = extra.getString("pseudo");
        System.out.println("Pseudo"+pseudo);
        Mouse = extra.getString("Mouse");
        timeSetted = extra.getInt("time");
        if(Mouse != null){
            if (Mouse.equals("yes")){
                isMouse = true;
            }
        }
    }
    public void initLayout(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior1.setPeekHeight(120);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior1.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    tapactionlayout.setVisibility(View.VISIBLE);
                }

                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    tapactionlayout.setVisibility(View.GONE);
                }

                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    tapactionlayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("id",id);
                intent.putExtra("pseudo",pseudo);
                startActivity(intent);
            }
        });
        tapactionlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBottomSheetBehavior1.getState()==BottomSheetBehavior.STATE_COLLAPSED)
                {
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });


        mapFragment.getMapAsync(this);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("id",id);
                intent.putExtra("pseudo",pseudo);
                startActivity(intent);
            }
        });
    }
    public void initView(){
        timer_tv = (TextView) findViewById(R.id.time_tv);
        bottomSheet = findViewById(R.id.bottom_sheet1);
        chat = (ImageView)findViewById(R.id.chatimg);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.chat);
        tapactionlayout = (LinearLayout) findViewById(R.id.tap_action_layout);
    }

    public String timeCalculate(int s){
        int m = 0;
        m = s/60;
        s = s - (m*60);
        return m+" : "+String.format("%02d",s);
    }

    // Toolbar and Menus
    public void displayActionBar() {
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    // Menu Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.themes, menu);
        return true;
    }

    // Option selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.normal:
                try {
                    boolean success =   mMap.setMapStyle
                            (MapStyleOptions.loadRawResourceStyle
                                    (this,R.raw.map_style));
                    if(!success)
                    {  Log.d(TAG,"Styles parsing failed"); }
                }catch (Resources.NotFoundException e)
                {
                    Log.d(TAG,"Styles not found",e);
                }
                return true;
            case R.id.dark:
                try {
                    boolean success =   mMap.setMapStyle
                            (MapStyleOptions.loadRawResourceStyle
                                    (this,R.raw.night_style));
                    if(!success)
                    {  Log.d(TAG,"Styles parsing failed"); }
                }catch (Resources.NotFoundException e)
                {
                    Log.d(TAG,"Styles not found",e);
                }
                return true;
            case R.id.light:
                try {
                    boolean success =   mMap.setMapStyle
                            (MapStyleOptions.loadRawResourceStyle
                                    (this,R.raw.light_style));
                    if(!success)
                    {  Log.d(TAG,"Styles parsing failed"); }
                }catch (Resources.NotFoundException e)
                {
                    Log.d(TAG,"Styles not found",e);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Default Theme
    public void normal(GoogleMap mMap){
        try {
            boolean success =   mMap.setMapStyle
                    (MapStyleOptions.loadRawResourceStyle
                            (this,R.raw.map_style));
            if(!success)
            {  Log.d(TAG,"Styles parsing failed"); }
        }catch (Resources.NotFoundException e)
        {
            Log.d(TAG,"Styles not found",e);
        }
    }

    public void addPlayer(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        // Create a new user with a first and last name
        player.setPlayer_id(fAuth.getUid());
        player.setEmail(fAuth.getCurrentUser().getEmail());
        player.setPseudo(pseudo);

        Map<String, Object> user = new HashMap<>();
        user.put("UID", player.getPlayer_id());
        user.put("Email", player.getEmail());
        user.put("Pseudo", player.getPseudo());
        user.put("Room_id",room_id);
        user.put("isMouse",isMouse);

        db.collection("player").document(player.getEmail()).set(user);
    }

    public void getTime(){
        myRef_initial = FirebaseDatabase.getInstance().getReference().child("gameRoom");
        myRef_initial.child(id).child("time").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    timeSetted = (int) (Long.parseLong(String.valueOf(task.getResult().getValue())) - System.currentTimeMillis()/1000);
                    Log.d("firebase111", String.valueOf(task.getResult().getValue()));
                }
            }
        });
    }

    public void onHideMousePositionClicked(View view){
        startHide = true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(timerReceiver);
        timerReceiver=null;
        //stopTimer();
    }

}