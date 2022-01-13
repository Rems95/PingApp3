package com.example.ping3.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ping3.models.Player_model;
import com.example.ping3.R;
import com.example.ping3.models.gameroom_model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;

public class GameroomActivity extends AppCompatActivity implements View.OnClickListener {

    String roomId, player,id,userID,pseudo;
    int time;
    TextView roomIdTV, playerTV,playersList,statusTV, playerConTV ;
    DatabaseReference myRef_initial, myRef_exit,myRef_players, myRef_players_full;
    FirebaseAuth fAuth;
    ImageView imageView;
    Button exitgameBtn,newPlayer, scanQR,go;
    public final static int QRcodeWidth = 500 ;
    Bitmap bitmap ;
    private Context mContext;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        roomIdTV = findViewById(R.id.roomIdTV);
        playerTV = findViewById(R.id.playerTV);
        playersList=findViewById(R.id.playersList);
        statusTV = findViewById(R.id.statusTV);
        playerConTV = findViewById(R.id.playersConTV);
        imageView = (ImageView)findViewById(R.id.imageView2);
        exitgameBtn = findViewById(R.id.exitgameBtn);
        scanQR = findViewById(R.id.scanQR);
        newPlayer=findViewById(R.id.newPlayer);
        go=findViewById(R.id.goToMaps);

        exitgameBtn.setOnClickListener(this);
        mContext = getApplicationContext();
        mActivity = GameroomActivity.this;

        System.out.println(id);
        Bundle extra = getIntent().getExtras();

        if (extra != null) {

            roomId = extra.getString("roomId");
            player = extra.getString("player");
            time = extra.getInt("time");
            roomIdTV.setText(roomId);
            playerTV.setText(player);


        myRef_initial = FirebaseDatabase.getInstance().getReference().child("gameRoom");
        myRef_initial.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    if ((ds.getValue(gameroom_model.class).getRoomId()).equals(Integer.parseInt(roomId))) {
                        id = ds.getKey();
                        myRef_players = FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id);
                        myRef_players.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<Player_model> Players= new ArrayList<Player_model>();

                                Players=snapshot.getValue(gameroom_model.class).getPlayers();
                                playersList.setText("");

                                for (int i=0;i<Players.size();i++){
                                    playersList.append(Players.get(i).getPseudo());
                                    playersList.append(" ");
                                    System.out.println(Players.get(i).getPseudo());

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
            if (fAuth.getCurrentUser() != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference usersRef = db.collection("users").document(fAuth.getCurrentUser().getUid());
                usersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                pseudo = (String) document.get("pseudo");
                            }
                        }
                    }
                });
            }
        }

        newPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent =   new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Insert Subject here");
                String app_url = "http://game.com/"+roomId;
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,app_url);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }}
        );
        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Link="http://game.com/"+roomId;

                try {
                    bitmap = TextToImageEncode(Link);

                    imageView.setImageBitmap(bitmap);

                } catch (WriterException e) {
                    e.printStackTrace();
                }

            }
        });
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameroomActivity.this, GameViewActivity.class);
                intent.putExtra("room_id",roomId);
                intent.putExtra("pseudo",pseudo);
                intent.putExtra("id",id);
                if(player.equals("MOUSE")){
                    intent.putExtra("Mouse","yes");
                }
                intent.putExtra("time",time);
                startActivity(intent);
            }
        });
    }


    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.exitgameBtn:
                final gameroom_model gr_exit = new gameroom_model();
                System.out.println("Key:"+id);
                myRef_exit = FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id);
                myRef_exit.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {

                        if ((ds.getValue(gameroom_model.class).getRoomId()).equals(Integer.parseInt(roomId))) {
                            final gameroom_model gr_exit = ds.getValue(gameroom_model.class);;
                            if (gr_exit.getCreator().equals(userID)) {

                                final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                builder.setTitle("Please confirm");
                                builder.setMessage("Are you sure you want to Close the Game Room and Exit the Game?");
                                builder.setCancelable(true);

                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(mContext,"Game Room Closed Successfully ! ",Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                     //   myRef_exit.removeValue();
                                    }
                                });

                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Do something when want to stay in the app
                                    }
                                });

                                // Create the alert dialog using alert dialog builder
                                AlertDialog dialog = builder.create();

                                // Finally, display the dialog when user press back button
                                dialog.show();



                            } else  {

                                final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                builder.setTitle("Please confirm");
                                builder.setMessage("Are you sure want to Exit the Game?");
                                builder.setCancelable(true);

                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        myRef_exit.setValue(gr_exit);
                                        Toast.makeText(mContext,"Game Room Closed Successfully ! ",Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                // Create the alert dialog using alert dialog builder
                                AlertDialog dialog = builder.create();

                                // Finally, display the dialog when user press back button
                                dialog.show();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                break;



        }
    }


    @Override
    public void onBackPressed(){

        final gameroom_model gr_exit = new gameroom_model();
        myRef_exit = FirebaseDatabase.getInstance().getReference().child("gameRoom").child(id);
        myRef_exit.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {

                if ((ds.getValue(gameroom_model.class).getRoomId()).equals(Integer.parseInt(roomId))) {
                    final gameroom_model gr_exit = ds.getValue(gameroom_model.class);;
                    if (player.equals("PLAYER 1")) {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle("Please confirm");
                        builder.setMessage("Are you sure you want to Close the Game Room and Exit the Game?");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(mContext,"Game Room Closed Successfully ! ",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                               // myRef_exit.removeValue();

                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Do something when want to stay in the app
                            }
                        });

                        // Create the alert dialog using alert dialog builder
                        AlertDialog dialog = builder.create();

                        // Finally, display the dialog when user press back button
                        dialog.show();


                    } else  {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle("Please confirm");
                        builder.setMessage("Are you sure want to Exit the Game?");
                        builder.setCancelable(true);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                myRef_exit.setValue(gr_exit);
                                Toast.makeText(mContext,"Game Room Closed Successfully ! ",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        // Create the alert dialog using alert dialog builder
                        AlertDialog dialog = builder.create();

                        // Finally, display the dialog when user press back button
                        dialog.show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
public void joinRoom(){

}
}



