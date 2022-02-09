package com.example.ping3.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.example.ping3.R;

public class GameRulesActivity extends AppCompatActivity {

    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_rules);

        webView = findViewById(R.id.rdg_web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("file:///android_asset/rdg.html");
    }
    public void exit_rdg(View view){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}