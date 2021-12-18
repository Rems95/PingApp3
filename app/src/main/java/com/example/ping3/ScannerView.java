package com.example.ping3;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerView extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView scannerView;
    DatabaseReference myRef_update,myRef_updatefull;
    String userID;
    String update_key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(ScannerView.this, "Please Press Back Button And Allow Camera Permission", Toast.LENGTH_SHORT).show();
                        scannerView.stopCamera();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void handleResult(Result rawResult) {
        {
            final String roomidx = rawResult.getText().replace("http://game.com/","");


                myRef_updatefull = FirebaseDatabase.getInstance().getReference().child("gameRoom");
                myRef_updatefull.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if ((ds.getValue(gameroom_model.class).getRoomId()).equals(Integer.parseInt(roomidx))) {

                                    gameroom_model gr = ds.getValue(gameroom_model.class);
                                    update_key = ds.getKey();
                                    myRef_update = FirebaseDatabase.getInstance().getReference().child("gameRoom").child(update_key);
                                    myRef_update.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot ds) {

                                            if ((ds.getValue(gameroom_model.class).getRoomId()).equals(Integer.parseInt(roomidx))) {
                                                gameroom_model gr_update = ds.getValue(gameroom_model.class);

//                                                    gr_update.setPlayer2(userID);
//                                                    gr_update.setStatus("Select a Move!");
                                                    myRef_update.setValue(gr_update);
                                                    Intent intent = new Intent(getApplicationContext(), gameRoom.class);
                                                    intent.putExtra("roomId", String.valueOf(gr_update.getRoomId()));
                                                    intent.putExtra("player", "PLAYER 2");
                                                    startActivity(intent);

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }

                            }


                        }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

}