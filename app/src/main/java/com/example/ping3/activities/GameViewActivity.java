package com.example.ping3.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ping3.R;
import com.example.ping3.models.Player_model;
import com.example.ping3.utils.TimerService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


/**
 * fail to add components in the Maps activity
 * so this game view activity is created
 * we can add boussole here
 * and chat room, skills, Time etc.
 * ---HUANG
 */
public class GameViewActivity extends AppCompatActivity {

    TextView timer_tv;
    TimerReceiver timerReceiver=null;
    String room_id,id,pseudo;
    String Mouse = null;
    int timeSetted;
    boolean isMouse = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    FirebaseAuth fAuth;
    Player_model player = new Player_model();
    double lastX = 0;
    double lastY = 0;
    int endHideTime = 0;
    boolean startHide = false;
    Button button_hide;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(GameViewActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        setContentView(R.layout.activity_game_view);

        initView();
        initTimeReceiver();
        //startService(new Intent(this, TimerService.class));
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

        addPlayer();
        getDeviceLocation();

    }

    //TimerReceiver
    public class TimerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals("com.demo.timer")) {
                int time = intent.getIntExtra("time", 0);
                timer_tv.setText(timeCalculate(timeSetted*60 -time));
                if((timeSetted*60 - time) == 0){
                    //Stop service
                    stopService(new Intent(GameViewActivity.this, TimerService.class));
                    unregisterReceiver(timerReceiver);
                    timerReceiver=null;
                    Toast.makeText(getApplicationContext(),"The room is over",Toast.LENGTH_SHORT).show();
                    Intent home = new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);;
                    startActivity(home);
                }
                if ( time % 2 == 0 && time != 0){
                    if (isMouse){
                        if(startHide){
                            if (endHideTime == 0){
                                endHideTime = time +10;
                                UpdatePosition(0,0);
                                button_hide.setEnabled(false);
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
                if(time % 5 == 0 && isMouse){
                    getOthersPosition();
                }
                if(time % 30 ==0 && !isMouse){
                    getMousePosition();
                }

            }
        }
    }

    public void OnButtonGoToMapClicked(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("room_id",room_id);
        intent.putExtra("time",timeSetted);
        intent.putExtra("pseudo",pseudo);
        intent.putExtra("id",id);
        if (isMouse){
            intent.putExtra("Mouse","yes");
        }
        else{
            intent.putExtra("lastX",lastX);
            intent.putExtra("lastY",lastY);
        }
        startActivity(intent);
    }

    public void OnButtonStartClicked(View view){
        if(timerReceiver==null){
            timerReceiver = new TimerReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.demo.timer");
            registerReceiver(timerReceiver, filter);
        }
        //start timer service
        startService(new Intent(this, TimerService.class));
    }

    public void OnButtonStopClicked(View view){
        //Stop service
        stopService(new Intent(this, TimerService.class));
        unregisterReceiver(timerReceiver);
        timerReceiver=null;
    }


    public void initView(){
        button_hide = (Button)findViewById(R.id.chat_button);
        timer_tv = (TextView) findViewById(R.id.Timer);
    }

    public void initTimeReceiver(){
        timerReceiver = new TimerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.demo.timer");
        registerReceiver(timerReceiver, filter);
    }

    public String timeCalculate(int s){
        int m = 0;
        m = s/60;
        s = s - (m*60);
        return m+" : "+String.format("%02d",s);
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
                        Toast.makeText(GameViewActivity.this,"Current location is null",Toast.LENGTH_SHORT).show();
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
        user.put("isMouse",isMouse);

        db.collection("player").document(player.getEmail()).set(user);
    }

    public void getOthersPosition(){
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
                            }
                        } else {
                            Toast.makeText(GameViewActivity.this,"Error getting documents."+task.getException(),Toast.LENGTH_LONG).show();
                            Log.w("TAG111", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void getMousePosition(){
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
                                lastX = x[0];
                                lastY = y[0];
                            }

                        } else {
                            Toast.makeText(GameViewActivity.this,"Error getting documents."+task.getException(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void onHideMousePositionClicked(View view){
        startHide = true;
    }

}