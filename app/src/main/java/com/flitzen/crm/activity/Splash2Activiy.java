package com.flitzen.crm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.flitzen.crm.R;
import com.flitzen.crm.utiles.SharePref;

public class Splash2Activiy extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2_activiy);

        sharedPreferences = SharePref.getSharePref(Splash2Activiy.this);

        Handler handler;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(sharedPreferences.getBoolean(SharePref.isLoggedIn,false)){
                    if(sharedPreferences.getBoolean(SharePref.isAddCompany,false)){
                        startActivity(new Intent(Splash2Activiy.this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                    else {
                        startActivity(new Intent(Splash2Activiy.this, AddCompanyActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                }
                else {
                    startActivity(new Intent(Splash2Activiy.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        }, 2000);
    }
}