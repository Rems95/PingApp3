package com.example.ping3.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ping3.models.Player_model;
import com.example.ping3.R;
import com.example.ping3.utils.GameRoomController;
import com.example.ping3.utils.ScannerView;
import com.example.ping3.models.gameroom_model;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    Button createRoom,joinRoom,ScanBtn,rejoin;
    FirebaseAuth fAuth;
    String userID,id,update_key;
    DatabaseReference myRef_initial;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        setContentView(R.layout.activity_main);
        createRoom = findViewById(R.id.createRoom);
        joinRoom = findViewById(R.id.joinRoom);
        ScanBtn = (Button) findViewById(R.id.joinRoom2);
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        rejoin = findViewById(R.id.createRoom4);


        ScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ScannerView.class));
            }

        });

        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                finish();
            }
        });

        rejoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReentrerClicked();
            }
        });

        joinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText joinRoomEV = new EditText(v.getContext());
                final AlertDialog.Builder joinRoomDialog = new AlertDialog.Builder(v.getContext());
                joinRoomDialog.setTitle("JOIN ROOM : ");
                joinRoomDialog.setMessage("ENTER ROOM LINK TO JOIN:");
                joinRoomDialog.setView(joinRoomEV);

                joinRoomDialog.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

//                        String regex = "[0-9]+";
                        final String Link = joinRoomEV.getText().toString();
                        final String roomidx = Link.replace("http://game.com/","");
                        GameRoomController gameRoomController = new GameRoomController();
                        gameRoomController.joinRoom(roomidx,MainActivity.this);


                    }
                });

                joinRoomDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                joinRoomDialog.create().show();

            }
        });

    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    public void cgu(View view){
        startActivity(new Intent(getApplicationContext(), CguActivity.class));
        //finish();
    }
    public void rdg(View view){
        startActivity(new Intent(getApplicationContext(), GameRulesActivity.class));
        //finish();
    }
    public void user(View view){
        startActivity(new Intent(getApplicationContext(), UserActivity.class));
        //finish();
    }



    public boolean checkroomId(int roomid , DataSnapshot dataSnapshot){
        final gameroom_model gr = new gameroom_model();


        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
            gr.setRoomId(snapshot.getValue(gameroom_model.class).getRoomId());

            if(gr.getRoomId().equals(roomid)){
                return false;
            }
    }
        return true;
    }

    public void onReentrerClicked(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String[] room_id = new String[1];
        final String[] id = new String[1];
        final Boolean[] isMouse = new Boolean[1];
        final long[] time = new long[1];
        final int[] status = new int[1];

        db.collection("player").whereEqualTo("UID",userID).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(MapsActivity.this,"OK",Toast.LENGTH_SHORT).show();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                room_id[0] = document.getData().get("Room_id").toString();
                                System.out.println("roomid="+room_id[0]);
                                isMouse[0] = document.getBoolean("isMouse");
                                System.out.println("isMouse"+isMouse[0]);
                            }
                        } else {
                            Toast.makeText(MainActivity.this,"Error getting documents."+task.getException(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

        myRef_initial = FirebaseDatabase.getInstance().getReference().child("gameRoom");
        myRef_initial.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    //Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    for (DataSnapshot ds :task.getResult().getChildren()){
                        if ((ds.getValue(gameroom_model.class).getRoomId()).equals(Integer.parseInt(room_id[0]))) {
                            id[0] = ds.getKey();
                            System.out.println("id" + id[0]);
                            time[0] = Long.parseLong(ds.child("time").getValue().toString());
                            System.out.println("Time" + time[0]);
                            status[0] = Integer.parseInt(ds.child("status").getValue().toString());
                            System.out.println("status" + status[0]);
                        }
                    }

                    System.out.println("last time"+(int)(time[0] - System.currentTimeMillis()/1000));
                    if (status[0] != 99){
                        if((int)(time[0] - System.currentTimeMillis()/1000) >= 0){
                            //startService(new Intent(getApplicationContext(), TimerService.class));
                            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                            intent.putExtra("room_id",room_id[0]);
                            intent.putExtra("pseudo","");
                            intent.putExtra("id",id[0]);
                            if(isMouse[0]){
                                intent.putExtra("Mouse","yes");
                            }
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Le jeu est fini",Toast.LENGTH_LONG).show();
                            //rejoin.setEnabled(false);
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Le jeu est fini",Toast.LENGTH_LONG).show();
                        //rejoin.setEnabled(false);
                    }

                    //Log.d("firebase", String.valueOf(task.getResult().getValue()));
                }
            }
        });



    }


}


