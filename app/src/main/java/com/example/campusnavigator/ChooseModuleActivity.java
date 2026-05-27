package com.example.campusnavigator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.campusnavigator.admin.AdminLoginActivity;
import com.example.campusnavigator.admin.CampusMapActivity;

public class ChooseModuleActivity extends AppCompatActivity {

    private CardView cardAdmin, cardUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_module);

        cardAdmin = findViewById(R.id.cardAdmin);
        cardUser = findViewById(R.id.cardUser);

        // Admin card click → animation + delayed activity
        cardAdmin.setOnClickListener(v -> playClickAnimation(v, AdminLoginActivity.class));

        // User card click → animation + delayed activity
        cardUser.setOnClickListener(v -> playClickAnimation(v, CampusMapActivity.class));
    }

    /**
     * Plays scale animation on a view and starts the target activity after animation ends.
     *
     * @param view          The view to animate
     * @param targetActivity The activity class to start after animation
     */
    private void playClickAnimation(View view, Class<?> targetActivity) {
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.card_click); // card_click.xml
        scaleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Start the target activity after animation ends
                Intent intent = new Intent(ChooseModuleActivity.this, targetActivity);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        view.startAnimation(scaleAnim);
    }
}
