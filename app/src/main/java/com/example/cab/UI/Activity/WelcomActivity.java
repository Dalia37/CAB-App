package com.example.cab.UI.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.cab.R;

public class WelcomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);
    }

    public void driverlogin(View view) {
        Intent intent = new Intent(getApplicationContext(),DriverLoginActivity.class);
        startActivity(intent);
    }

    public void customerlogin(View view) {
        Intent intent = new Intent(getApplicationContext(),CustomerLoginActivity.class);
        startActivity(intent);
    }
}
