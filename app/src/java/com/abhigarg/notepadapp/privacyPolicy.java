package com.abhigarg.notepadapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

import com.abhigarg.notepadapp.R;

public class privacyPolicy extends AppCompatActivity {
    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        web=(WebView)findViewById(R.id.webView);
        web.loadUrl("file:///android_asset/privacy.html");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
