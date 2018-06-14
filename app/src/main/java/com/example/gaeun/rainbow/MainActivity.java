package com.example.gaeun.rainbow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.net.URL;

public class MainActivity extends AppCompatActivity {
    static boolean splash_plug=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(splash_plug){
            startActivity(new Intent(this,SplashActivity.class));
            splash_plug=false;
        }
    }


    public void ck_start(View v){
        Intent intent = new Intent(this, Rainbow_start.class);
        startActivity(intent);
        finish();
    }

    public void ck_camera(View v){
        Intent intent = new Intent(this, Rainbow_camera.class);
        startActivity(intent);
        finish();
    }

    public void ck_about(View v){
        Intent intent = new Intent(this, Rainbow_about.class);
        startActivity(intent);
        finish();
    }

    public void ck_close(View v){
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
