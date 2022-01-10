package com.example.ping3.utils;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ping3.activities.GameroomActivity;
import com.example.ping3.models.Player_model;
import com.example.ping3.models.gameroom_model;
import com.google.firebase.auth.FirebaseAuth;
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
    Player_model player = new Player_model();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

            GameRoomController gameRoomController = new GameRoomController();
            gameRoomController.joinRoom(roomidx,ScannerView.this);


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