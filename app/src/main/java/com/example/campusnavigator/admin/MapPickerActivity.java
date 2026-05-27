
package com.example.campusnavigator.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campusnavigator.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.Arrays;
import java.util.List;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LatLng selectedLatLng;

    Button btnConfirm;
    TextView tvSelectedCoords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        btnConfirm = findViewById(R.id.btnConfirmLocation);
        tvSelectedCoords = findViewById(R.id.tvSelectedCoords);

        // Initialize map fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;

        // Define Vishwakarma University Pune campus boundary
        List<LatLng> campusBoundary = Arrays.asList(
                new LatLng(18.456896, 73.8832645),
                new LatLng(18.457027, 73.8833262),
                new LatLng(18.4578043, 73.8836842),
                new LatLng(18.4585549, 73.883644),
                new LatLng(18.4595725, 73.8834884),
                new LatLng(18.4600916, 73.8828286),
                new LatLng(18.4608213, 73.8822961),
                new LatLng(18.4609333, 73.8833502),
                new LatLng(18.4608748, 73.8839563),
                new LatLng(18.4606738, 73.8842152),
                new LatLng(18.4605109, 73.8840864),
                new LatLng(18.4601245, 73.8840062),
                new LatLng(18.45994, 73.8841242),
                new LatLng(18.4599118, 73.8842433),
                new LatLng(18.4605593, 73.8844351),
                new LatLng(18.4606674, 73.8849032),
                new LatLng(18.4607043, 73.8855241),
                new LatLng(18.4601738, 73.8857776),
                new LatLng(18.4598762, 73.8859465),
                new LatLng(18.4593533, 73.8856099),
                new LatLng(18.459247, 73.8841272),
                new LatLng(18.4589512, 73.8841956),
                new LatLng(18.4579665, 73.8843552),
                new LatLng(18.4570786, 73.8843002),
                new LatLng(18.4568382, 73.8839368),
                new LatLng(18.4567275, 73.8836029),
                new LatLng(18.456896, 73.8832645)
        );

        // Center the map view
        LatLng center = new LatLng(18.4590, 73.8840);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 17f));

        // Dotted red border
        List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(10));
        googleMap.addPolyline(new PolylineOptions()
                .addAll(campusBoundary)
                .color(Color.RED)
                .width(5)
                .pattern(pattern));

        // Transparent fill inside
        googleMap.addPolygon(new PolygonOptions()
                .addAll(campusBoundary)
                .strokeColor(Color.TRANSPARENT)
                .fillColor(Color.argb(30, 255, 0, 0))); // translucent red

        // Enable zoom buttons
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Listen for user tap
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLatLng = latLng;

            tvSelectedCoords.setText(
                    String.format("Lat: %.6f, Lng: %.6f", latLng.latitude, latLng.longitude)
            );
            btnConfirm.setEnabled(true);

            btnConfirm.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLatLng.latitude);
                resultIntent.putExtra("longitude", selectedLatLng.longitude);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            });
        });
    }
}
