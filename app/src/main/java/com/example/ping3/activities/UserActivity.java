package com.example.ping3.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ping3.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
String pseudo;
TextInputEditText set_pseudo,email;
Button mod_pseudo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        set_pseudo = findViewById(R.id.set_pseudo);
        email=findViewById(R.id.view_email);
        mod_pseudo=findViewById(R.id.mod_pseudo);
        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference usersRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            usersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            pseudo = (String) document.get("pseudo");
                            set_pseudo.setText(pseudo);
                        }
                    }
                }
            });

        }

        mod_pseudo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Map<String, Object> map = new HashMap<>();
                map.put("pseudo", "" + set_pseudo.getText().toString());
                db.collection("users").document(user.getUid().toString()).set(map);
                Toast.makeText(UserActivity.this, "Pseudo enrgistré", Toast.LENGTH_SHORT).show();

            }
        });

    }

    public void exit_user(View view){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

}