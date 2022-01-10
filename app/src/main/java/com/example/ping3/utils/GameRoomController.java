package com.example.ping3.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.ping3.activities.GameroomActivity;
import com.example.ping3.activities.MainActivity;
import com.example.ping3.models.Player_model;
import com.example.ping3.models.gameroom_model;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameRoomController {
    DatabaseReference myRef,myRef_update,myRef_updatefull;
    String userID,id,update_key;
    Player_model player = new Player_model();


    public void joinRoom(String roomidx, Context context){

        if (TextUtils.isEmpty(roomidx)) {
            Toast.makeText(context,"Room ID is required !",Toast.LENGTH_SHORT).show();
            return;

        } else {

            myRef_updatefull = FirebaseDatabase.getInstance().getReference().child("gameRoom");
            myRef_updatefull.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    boolean flag = checkroomId(Integer.parseInt(roomidx), snapshot);
                    if (!flag) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if ((ds.getValue(gameroom_model.class).getRoomId()).equals(Integer.parseInt(roomidx))) {
                                update_key = ds.getKey();
                                myRef_update = FirebaseDatabase.getInstance().getReference().child("gameRoom").child(update_key);
                                myRef_update.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot ds) {

                                        if ((ds.getValue(gameroom_model.class).getRoomId()).equals(Integer.parseInt(roomidx))) {
                                            gameroom_model gr_update = ds.getValue(gameroom_model.class);
                                            if ((gr_update.getCreator()).equals(userID)) {
                                                Intent intent = new Intent(context, GameroomActivity.class);
                                                intent.putExtra("roomId", String.valueOf(gr_update.getRoomId()));
                                                intent.putExtra("player",player.getPseudo() );
                                                intent.putExtra("id",update_key);
                                                context.startActivity(intent);

                                            } else {
                                                player.setPlayer_id(userID);
                                                player.setPseudo("Cat");
                                                if(gr_update.getPlayers().size()<gr_update.getNbPlayers()){
                                                gr_update.addPlayers(player);
                                                }else{
                                                    Toast.makeText(context,"The room is full",Toast.LENGTH_SHORT).show();
                                                }
                                                System.out.println("Player:"+player);
                                                myRef_update.setValue(gr_update);
                                                Intent intent = new Intent(context, GameroomActivity.class);
                                                intent.putExtra("roomId", String.valueOf(gr_update.getRoomId()));
                                                intent.putExtra("player",player.getPseudo() );
                                                intent.putExtra("id",update_key);
                                                context.startActivity(intent);

                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }


                    }
                    else{
                        Toast.makeText(context,"Room ID Does Not Exist !",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
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
