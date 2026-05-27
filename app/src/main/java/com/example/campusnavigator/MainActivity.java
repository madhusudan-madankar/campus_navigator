package com.example.campusnavigator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar / Logo image to rotate
        ImageView logo = findViewById(R.id.logo);


        // Move to ChooseModuleActivity after delay
        new Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, ChooseModuleActivity.class));
            finish();
        }, SPLASH_DELAY);
    }
}
