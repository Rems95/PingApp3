package com.example.ping3.activities;

import static android.view.View.INVISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ping3.R;
import com.example.ping3.models.Player_model;
import com.example.ping3.utils.Compass;
import com.example.ping3.utils.GPSTracker;
import com.example.ping3.utils.TimerService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.google.maps.android.SphericalUtil;


import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Toolbar myToolbar;
    private FirebaseAnalytics mFirebaseAnalytics;
    private final int imageSize = 120;
    private static final String TAG = "MapActivity";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    FirebaseAuth fAuth;
    CardView cardView1;
    CardView cardView2;
    Player_model player = new Player_model();
    String room_id,id,pseudo;
    TimerReceiver timerReceiver = null;
    String Mouse = null;
    boolean isMouse = false;
    int timeSetted;
    private Compass compass;
    private ImageView arrowViewt;
    private TextView text_atas;
    private float currentAzimuth;
    SharedPreferences prefs;
    GPSTracker gps;
    int lastTime;
    FloatingActionButton floatingActionButton;
    private BottomSheetBehavior mBottomSheetBehavior1;
    LinearLayout tapactionlayout;
    View bottomSheet;
    ImageView chat;
    TextView timer_tv;
    int endHideTime = 0 , endFaultTime = 0 , endNoChatTime = 0;
    boolean startHide = false , startFault = false , startNoChat = false;
    DatabaseReference myRef_initial,myRef_status,myRef_level;
    double faultX = 0, faultY = 0;
    double mousepositionx=0, mousepositiony = 0;
    Handler handler = new Handler();
    Runnable runnable;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        fAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_maps);
        initView();
        getExtra();
        initLayout();
        getTime();
        setMapTheme();
        initTimeReceiver();
        startTimer();
        addPlayer();
        getDeviceLocation();
        //initial game room status chagne listener, 99 = cat win
        myRef_status = FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id).child("status");
        myRef_status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Integer.parseInt(snapshot.getValue().toString()) == 99){
                    stopTimer();
                    timerReceiver = null;
                    Toast.makeText(getApplicationContext(), "Les chats gagnent!!!", Toast.LENGTH_LONG).show();
                    Intent home = new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(home);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        prefs = getSharedPreferences("", MODE_PRIVATE);
        gps = new GPSTracker(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        arrowViewt = (ImageView) findViewById(R.id.arrow);
        text_atas = (TextView) findViewById(R.id.teks_atas);
        arrowViewt .setVisibility(INVISIBLE);
        arrowViewt .setVisibility(View.GONE);

        setupCompass();

        startTimer();
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
                                faultX = x[0];
                                faultY = y[0];
                                LatLng pin = new LatLng(x[0], y[0]);
                                mousepositionx=faultX;
                                mousepositiony=faultY;
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(pin)
                                        .title("Souris").icon(BitmapFromVector(getApplicationContext(), R.drawable.markercat));
                                mMap.addMarker(markerOptions);
                            }
                        } else {
                            Toast.makeText(MapsActivity.this,"Error getting documents."+task.getException(),Toast.LENGTH_LONG).show();
                            Log.w("TAG111", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void setupCompass() {
        Boolean permission_granted = GetBoolean("permission_granted");
        if(permission_granted) {
            getBearing();
        }else{
            text_atas.setText(getResources().getString(R.string.msg_permission_not_granted_yet));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        }



        compass = new Compass(this);
        Compass.CompassListener cl = new Compass.CompassListener() {

            @Override
            public void onNewAzimuth(float azimuth) {
                // adjustArrow(azimuth);
                adjustArrowt(azimuth);
            }
        };
        compass.setListener(cl);
    }

    public void adjustArrowt(float azimuth) {
        //Log.d(TAG, "will set rotation from " + currentAzimuth + " to "                + azimuth);

        float kiblat_derajat = GetFloat("kiblat_derajat");
        Animation an = new RotateAnimation(-(currentAzimuth)+kiblat_derajat, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = (azimuth);
        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        arrowViewt.startAnimation(an);
        if(kiblat_derajat > 0){
            arrowViewt .setVisibility(View.VISIBLE);
        }else{
            arrowViewt .setVisibility(INVISIBLE);
            arrowViewt .setVisibility(View.GONE);
        }
    }

    public Boolean GetBoolean(String Judul){
        Boolean result = prefs.getBoolean(Judul, false);
        return result;
    }
    public void fetch_GPS(){
        final double[] result = {0};
        gps = new GPSTracker(this);
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            // \n is for new line
            // Toast.makeText(getApplicationContext(), "Lokasi anda: - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            Log.e("TAG", "GPS is on");
            double lat_saya = gps.getLatitude ();
            double lon_saya = gps.getLongitude ();
            if(lat_saya < 0.001 && lon_saya < 0.001) {
                // arrowViewt.isShown(false);
                arrowViewt .setVisibility(INVISIBLE);
                arrowViewt .setVisibility(View.GONE);
                text_atas.setText("En attente ...");

                // Toast.makeText(getApplicationContext(), "Location not ready, Please Restart Application", Toast.LENGTH_LONG).show();
            }else{

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                final double[] x = new double[1];
                final double[] y = new double[1];
                float zoomLevel = 15.0f;
                db.collection("player").whereEqualTo("Room_id",room_id).whereNotEqualTo("isMouse",true)
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
                                        double longitude2 = pin.longitude;
                                        System.out.println(longitude2);
                                        double longitude1 = lon_saya;
                                        double latitude2 = Math.toRadians(pin.latitude);
                                        double latitude1 = Math.toRadians(lat_saya);
                                        double longDiff= Math.toRadians(longitude2-longitude1);
                                        double y= Math.sin(longDiff)*Math.cos(latitude2);
                                        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);
                                        result[0] = (Math.toDegrees(Math.atan2(y, x))+360)%360;
                                        float result2 = (float) result[0];
                                        Double distance;
                                        LatLng currentpos = new LatLng(latitude, longitude);
                                        LatLng mousepos = pin;
                                        System.out.println(pin);
                                        distance = SphericalUtil.computeDistanceBetween(currentpos, mousepos);

                                        SaveFloat("kiblat_derajat", result2);
                                        if(distance<1000){
                                            text_atas.setText(String.format("%.1f", distance ) + "m");
                                            if(distance<300){
                                                text_atas.setText("A proximité");
                                            }
                                        }
                                        else{
                                            text_atas.setText(String.format("%.1f", distance / 1000) + "km");
                                        }
                                        arrowViewt .setVisibility(View.VISIBLE);

                                    }
                                } else {
                                    Toast.makeText(MapsActivity.this,"Error getting documents."+task.getException(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });


            }
            //  Toast.makeText(getApplicationContext(), "lat_saya: "+lat_saya + "\nlon_saya: "+lon_saya, Toast.LENGTH_LONG).show();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();

            // arrowViewt.isShown(false);
            arrowViewt .setVisibility(INVISIBLE);
            arrowViewt .setVisibility(View.GONE);
            text_atas.setText(getResources().getString(R.string.pls_enable_location));

            // Toast.makeText(getApplicationContext(), "Please enable Location first and Restart Application", Toast.LENGTH_LONG).show();
        }
    }

    public void SaveFloat(String Judul, Float bbb){
        SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat(Judul, bbb);
        edit.apply();
    }
    public Float GetFloat(String Judul){
        Float xxxxxx = prefs.getFloat(Judul, 0);
        return xxxxxx;
    }
    @SuppressLint("MissingPermission")
    public void getBearing(){
        // Get the location manager

        float kiblat_derajat = GetFloat("kiblat_derajat");
        if(kiblat_derajat > 0.0001){

            arrowViewt .setVisibility(View.VISIBLE);
        }else
        {
            fetch_GPS();
        }
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
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(pin)
                                        .title("Souris").icon(BitmapFromVector(getApplicationContext(), R.drawable.markermouse));
                                mMap.addMarker(markerOptions);
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

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onResume() {
        if(compass != null) {
            compass.start();
        }
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, 1000);
                fetch_GPS();

            }
        }, 1000);
        super.onResume();
    }
    //TimerReceiver
    public class TimerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals("com.demo.timer")) {
                int time = intent.getIntExtra("time", 0);
                timer_tv.setText(timeCalculate(timeSetted - time));
                lastTime = timeSetted - time;
                if (time != 0) {
                    if (lastTime == 0) {
                        stopTimer();
                        //unregisterReceiver(timerReceiver);
                        timerReceiver = null;
                        Toast.makeText(getApplicationContext(), "The room is over", Toast.LENGTH_SHORT).show();
                        Intent home = new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(home);
                    }
                    if (lastTime % 5 == 0 && isMouse) {
                        getOthersPosition();
                    }
                    if (lastTime % 300 == 0 && !isMouse) {
                        getMousePosition();
                        fetch_GPS();
                    }
                    if (lastTime % 3 == 0) {
                        if (isMouse) {
                            if (startHide) {
                                if (endHideTime == 0) {
                                    Toast.makeText(getApplicationContext(),"Vous commencez à cacher.",Toast.LENGTH_LONG).show();
                                    endHideTime = time + 320;
                                    UpdatePosition(0, 0);
                                } else if (endHideTime > time) {
                                    UpdatePosition(0, 0);
                                } else {
                                    Toast.makeText(getApplicationContext(),"Vous ne pouvez plus vous cacher.",Toast.LENGTH_LONG).show();
                                    startHide = false;
                                    getDeviceLocation();
                                }
                            } else if (startFault) {
                                if (endFaultTime == 0) {
                                    Toast.makeText(getApplicationContext(),"Vous commencez à fournir le mauvais emplacement.",Toast.LENGTH_LONG).show();
                                    endFaultTime = time + 320;
                                    UpdatePosition(faultX, faultY);
                                } else if (endFaultTime > time) {
                                    UpdatePosition(faultX, faultY);
                                } else {
                                    Toast.makeText(getApplicationContext(),"Vous ne pouvez plus donner de fausses informations",Toast.LENGTH_LONG).show();
                                    startFault = false;
                                    getDeviceLocation();
                                }
                            } else {
                                getDeviceLocation();
                            }
                            if (startNoChat){
                                if (endNoChatTime == 0) {
                                    Toast.makeText(getApplicationContext(),"Vous avez commencé à désactiver le chat.",Toast.LENGTH_LONG).show();
                                    endNoChatTime = time + 320;
                                    myRef_status.setValue(98);
                                } else if (endNoChatTime <= time) {
                                    Toast.makeText(getApplicationContext(),"Vous ne pouvez plus désactiver le chat.",Toast.LENGTH_LONG).show();
                                    myRef_status.setValue(2);
                                }
                            }
                        } else {
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
        //System.out.println("Pseudo"+pseudo);
        Mouse = extra.getString("Mouse");
        //timeSetted = extra.getInt("time");
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
        if(!isMouse){
            cardView1.setVisibility(INVISIBLE);
        }
        else{
            cardView2.setVisibility(INVISIBLE);

        }

    }
    public void initView(){
        timer_tv = (TextView) findViewById(R.id.time_tv);
        bottomSheet = findViewById(R.id.bottom_sheet1);
        cardView1= findViewById(R.id.cardview1);
        cardView2= findViewById(R.id.cardview2);
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


    // Menu Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.themes, menu);
        return true;
    }

    // Option selected
    public void setMapTheme() {
        myRef_level = FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id).child("level");
        myRef_level.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println(snapshot.getValue().toString());

                if (snapshot.getValue().toString().equals("FACILE")) {
                    System.out.println("nice1");
                    try {
                        boolean success = mMap.setMapStyle
                                (MapStyleOptions.loadRawResourceStyle
                                        (MapsActivity.this, R.raw.map_style));
                        if (!success) {
                            Log.d(TAG, "Styles parsing failed");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.d(TAG, "Styles not found", e);
                    }
                }else if(snapshot.getValue().toString().equals("MOYEN")) {
                    System.out.println("nice2");

                    try {
                        boolean success = mMap.setMapStyle
                                (MapStyleOptions.loadRawResourceStyle
                                        (MapsActivity.this, R.raw.light_style));
                        if (!success) {
                            Log.d(TAG, "Styles parsing failed");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.d(TAG, "Styles not found", e);
                    }
                }else if(snapshot.getValue().toString().equals("DIFFICILE")) {
                    System.out.println("nice3");

                    try {
                        boolean success = mMap.setMapStyle
                                (MapStyleOptions.loadRawResourceStyle
                                        (MapsActivity.this, R.raw.night_style));
                        if (!success) {
                            Log.d(TAG, "Styles parsing failed");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.d(TAG, "Styles not found", e);
                    }
                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        user.put("X",0);
        user.put("Y",0);

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
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start compass");
        if(compass != null) {
            compass.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(compass != null) {
            compass.stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        if(compass != null) {
            compass.stop();
        }
    }
    public void onHideMousePositionClicked(View view){
        startHide = true;
    }

    public void onFaultMousePositionClicked(View view){
        startFault = true;
    }

    public void onNoChatClicked(View view){
        startNoChat = true;
    }

    public void onAttrapeClicked(View view){
        if (isMouse){
            myRef_status.setValue(99);
        }
    }

}