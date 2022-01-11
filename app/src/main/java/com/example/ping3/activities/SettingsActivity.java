package com.example.ping3.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import com.example.ping3.R;
import com.example.ping3.models.Player_model;
import com.example.ping3.models.gameroom_model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SettingsActivity extends AppCompatActivity {
    private Spinner timeSpinner,nbPlayersSpinner,levelSpinner;
    private Button saveButton;
    DatabaseReference myRef;
    String id,userID;

    Player_model player = new Player_model();

    // create setting page
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        timeSpinner = findViewById(R.id.timeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);



        nbPlayersSpinner = findViewById(R.id.numberofplayersSpinner);
        ArrayAdapter<CharSequence> boardSizeAdapter = ArrayAdapter.createFromResource(this,
                R.array.nbplayers, android.R.layout.simple_spinner_item);
        boardSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nbPlayersSpinner.setAdapter(boardSizeAdapter);

        levelSpinner = findViewById(R.id.levelSpinner);
        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(this,
                R.array.level, android.R.layout.simple_spinner_item);
        boardSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(levelAdapter);




        // config save button
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int time = Integer.parseInt(timeSpinner.getSelectedItem().toString());
                int nbPlayers = Integer.parseInt(nbPlayersSpinner.getSelectedItem().toString());
                String level = levelSpinner.getSelectedItem().toString();
                final int roomId = genRoomid();
                System.out.println(FirebaseDatabase.getInstance().getReference());
                myRef = FirebaseDatabase.getInstance().getReference().child("gameRoom");

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        gameroom_model gr_push = new gameroom_model();
                        player.setPlayer_id(userID);
                        player.setPseudo("MOUSE");
                        gr_push.setCreator(userID);
                        gr_push.setStatus(1);
                        gr_push.setRoomId(roomId);
                        gr_push.setNbPlayers(nbPlayers);
                        gr_push.setTime(time);
                        gr_push.setLevel(level);
                        gr_push.addPlayers(player);
                        myRef.push().setValue(gr_push);

                        Intent intent = new Intent(getApplicationContext(), GameroomActivity.class);
                        intent.putExtra("roomId", String.valueOf(gr_push.getRoomId()));
                        intent.putExtra("player",player.getPseudo());
                        intent.putExtra("time",time);
                        intent.putExtra("nbPlayers",nbPlayers);
                        intent.putExtra("level",level);
                        intent.putExtra("id",id);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        // config main button
    }
    public int genRoomid (){
        int n = 10000 + new Random().nextInt(90000);
        return(n);
    }
}



