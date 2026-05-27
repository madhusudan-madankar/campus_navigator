package com.example.campusnavigator.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.campusnavigator.R;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardAddLocation, cardManageConnections, cardViewLocations, cardViewMap;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminDashboard_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize CardViews
        cardAddLocation = findViewById(R.id.adminDashboard_card_addLocation);
        cardManageConnections = findViewById(R.id.adminDashboard_card_manageConnections);
        cardViewLocations = findViewById(R.id.adminDashboard_card_viewLocations);
        cardViewMap = findViewById(R.id.adminDashboard_card_viewMap);
        btnLogout = findViewById(R.id.adminDashboard_btn_logout);

        // Set click animations with delayed activity start
        cardAddLocation.setOnClickListener(v ->
                playClickAnimation(v, AddLocationActivity.class)
        );

        cardManageConnections.setOnClickListener(v ->
                playClickAnimation(v, ManageConnectionsActivity.class)
        );

        cardViewLocations.setOnClickListener(v ->
                playClickAnimation(v, ViewLocationsActivity.class)
        );

        cardViewMap.setOnClickListener(v ->
                playClickAnimation(v, CampusMapActivity.class)
        );

        // Logout button
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Plays scale animation on a view and starts the target activity after animation ends.
     *
     * @param view          The view to animate
     * @param targetActivity The activity class to start after animation
     */
    private void playClickAnimation(View view, Class<?> targetActivity) {
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.card_click); // scale.xml
        scaleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(AdminDashboardActivity.this, targetActivity);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        view.startAnimation(scaleAnim);
    }
}
