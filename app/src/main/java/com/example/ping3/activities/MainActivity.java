package com.example.ping3.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    Button createRoom,joinRoom,ScanBtn;
    FirebaseAuth fAuth;
    String userID,id,update_key;



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


}


