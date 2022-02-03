package com.example.ping3.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ping3.R;
import com.google.firebase.auth.FirebaseAuth;

public class CguActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cgu);
    }

    public void exit_cgu(View view){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

}