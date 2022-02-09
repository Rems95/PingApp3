package com.example.ping3.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.example.ping3.R;
import com.google.firebase.auth.FirebaseAuth;


public class CguActivity extends AppCompatActivity {
    WebView webView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cgu);

        webView = findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("file:///android_asset/cgu.html");

    }
    private class MyBrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.equals("Navigation://OpenNativeScreen")) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                return true;
            }
            return false;
        }
    }

    public void exit_cgu(View view){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

}