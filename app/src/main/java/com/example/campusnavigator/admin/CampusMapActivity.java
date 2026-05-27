package com.example.campusnavigator.admin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.example.campusnavigator.R;
import com.example.campusnavigator.firebase.constants.FirebaseConstants;
import com.example.campusnavigator.firebase.model.ConnectionModel;
import com.example.campusnavigator.firebase.model.LocationModel;
import com.example.campusnavigator.routing.PathFinder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CampusMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocation;
    private Marker userMarker = null;
    private final List<LocationModel> locationList = new ArrayList<>();
    private final Map<String, LocationModel> locationByKey = new HashMap<>(); // nameKey -> LocationModel (for UI autocomplete)
    private final Map<String, LocationModel> locationMapById = new HashMap<>(); // id -> LocationModel (for graph)
    private final List<ConnectionModel> connectionList = new ArrayList<>();

    private AutoCompleteTextView sourceInput, destinationInput;
    private Button btnFindRoute;
    private AppCompatButton btnStartNavigation;
    private AlertDialog loadingDialog;
    private Marker distanceMarker;               // <- add this
    private Polyline currentPolyline;
    private static final int REQ_LOCATION = 101;
    private final PathFinder pathFinder = new PathFinder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        db = FirebaseFirestore.getInstance();
        sourceInput = findViewById(R.id.sourceInput);
        destinationInput = findViewById(R.id.destinationInput);
        btnFindRoute = findViewById(R.id.btnFindRoute);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);


        btnStartNavigation.setOnClickListener(v -> {
            startLiveNavigation();
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        loadingDialog = createLoadingDialog();

        btnFindRoute.setOnClickListener(v -> onFindRouteClicked());
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);



    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        loadLocations();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION
            );
            return;
        }

        enableUserLocation();

    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    // ----------------- LOAD LOCATIONS -----------------
    private void loadLocations() {
        showLoading("Loading campus locations...");
        db.collection(FirebaseConstants.COLLECTION_LOCATIONS)
                .get()
                .addOnSuccessListener(this::onLocationsLoaded)
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Failed to load locations", Toast.LENGTH_SHORT).show();
                });
    }

    private void onLocationsLoaded(QuerySnapshot snapshot) {
        locationList.clear();
        locationByKey.clear();
        locationMapById.clear();

        if (snapshot != null) {
            for (DocumentSnapshot d : snapshot.getDocuments()) {
                LocationModel loc = d.toObject(LocationModel.class);
                if (loc == null) continue;

                // Ensure id and nameKey are set
                loc.setId(d.getId());
                if (loc.getNameKey() == null || loc.getNameKey().trim().isEmpty()) {
                    if (loc.getName() != null) loc.setNameKey(loc.getName().toLowerCase().trim());
                } else {
                    loc.setNameKey(loc.getNameKey().toLowerCase().trim());
                }

                locationList.add(loc);
                locationByKey.put(loc.getNameKey(), loc);
                locationMapById.put(loc.getId(), loc);
            }
        }
        drawAllMarkers();
        loadConnections();
    }

    // ----------------- LOAD CONNECTIONS -----------------
    private void loadConnections() {
        showLoading("Loading connections...");
        db.collection(FirebaseConstants.COLLECTION_CONNECTIONS)
                .get()
                .addOnSuccessListener(this::onConnectionsLoaded)
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Failed to load connections", Toast.LENGTH_SHORT).show();
                });
    }

    private void onConnectionsLoaded(QuerySnapshot snapshot) {
        connectionList.clear();

        if (snapshot != null) {
            for (DocumentSnapshot d : snapshot.getDocuments()) {
                ConnectionModel conn = d.toObject(ConnectionModel.class);
                if (conn == null) continue;

                // If Firestore doc didn't include id field, set it
                if (conn.getId() == null || conn.getId().isEmpty()) {
                    conn.setId(d.getId());
                }

                // BACKWARD COMPATIBILITY:
                // If connection was created with name-keys (legacy: sourceKey/destKey storing names),
                // attempt to map those names to location IDs. Otherwise prefer explicit sourceId/destId fields.
                String srcId = conn.getSourceId();
                String dstId = conn.getDestId();

                // If sourceId is empty but sourceKey contains a name (legacy), try resolving
                if ((srcId == null || srcId.trim().isEmpty()) && conn.getSourceKey() != null) {
                    String maybeNameKey = conn.getSourceKey().toLowerCase().trim();
                    LocationModel resolved = locationByKey.get(maybeNameKey);
                    if (resolved != null) {
                        srcId = resolved.getId();
                        conn.setSourceId(srcId);
                    }
                }

                // If destId is empty but destKey contains a name (legacy), try resolving
                if ((dstId == null || dstId.trim().isEmpty()) && conn.getDestKey() != null) {
                    String maybeNameKey = conn.getDestKey().toLowerCase().trim();
                    LocationModel resolved = locationByKey.get(maybeNameKey);
                    if (resolved != null) {
                        dstId = resolved.getId();
                        conn.setDestId(dstId);
                    }
                }

                // Add connection (those without resolvable IDs will be ignored when building graph)
                connectionList.add(conn);
            }
        }

        // Build graph using IDs
        pathFinder.buildGraph(locationList, connectionList);

        populateAutoComplete();
        hideLoading();
    }

    // ----------------- AUTOCOMPLETE -----------------
    private void populateAutoComplete() {
        List<String> names = new ArrayList<>();
        for (LocationModel l : locationList) names.add(l.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, names);
        sourceInput.setAdapter(adapter);
        destinationInput.setAdapter(adapter);
    }

    // ----------------- ROUTE BUTTON -----------------
    private void onFindRouteClicked() {
        String srcName = sourceInput.getText().toString().trim().toLowerCase();
        String dstName = destinationInput.getText().toString().trim().toLowerCase();

        if (srcName.isEmpty() || dstName.isEmpty()) {
            Toast.makeText(this, "Please enter source and destination", Toast.LENGTH_LONG).show();
            return;
        }

        LocationModel srcLoc = locationByKey.get(srcName);
        LocationModel dstLoc = locationByKey.get(dstName);

        if (srcLoc == null || dstLoc == null) {
            Toast.makeText(this, "Invalid Source or Destination", Toast.LENGTH_LONG).show();
            return;
        }

        String srcId = srcLoc.getId();
        String dstId = dstLoc.getId();

        showLoading("Finding best route...");

        new Thread(() -> {
            List<String> nodeIdPath = pathFinder.findShortestPath(srcId, dstId);

            runOnUiThread(() -> {
                hideLoading();

                if (nodeIdPath == null || nodeIdPath.isEmpty()) {
                    Toast.makeText(this, "No route found", Toast.LENGTH_LONG).show();
                    return;
                }

                List<LocationModel> locationPath = new ArrayList<>();
                for (String id : nodeIdPath) {
                    LocationModel lm = locationMapById.get(id);
                    if (lm != null) locationPath.add(lm);
                }

                // calculate distance using stored connections (matching by IDs)
                double totalDistance = calculatePathDistance(nodeIdPath);
                drawPathOnMap(locationPath, totalDistance);

                Toast.makeText(this,
                        "Path found: " + String.format("%.2f", totalDistance) + " meters",
                        Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    // ----------------- DISTANCE CALCULATION -----------------
    private double calculatePathDistance(List<String> path) {
        double total = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {

            String fromId = path.get(i);
            String toId = path.get(i + 1);

            if (fromId == null || toId == null) continue;

            for (ConnectionModel c : connectionList) {
                // prefer matching by explicit IDs if available
                String cSrc = c.getSourceId();
                String cDst = c.getDestId();

                if (cSrc != null && cDst != null) {
                    if ((cSrc.equals(fromId) && cDst.equals(toId)) ||
                            (cSrc.equals(toId) && cDst.equals(fromId))) {
                        total += c.getDistanceMeters();
                        break;
                    }
                } else {
                    // fallback: if connection stores legacy name keys (sourceKey/destKey),
                    // compare using names mapped from locationMapById
                    String fNameKey = locationMapById.get(fromId) != null ? locationMapById.get(fromId).getNameKey() : null;
                    String tNameKey = locationMapById.get(toId) != null ? locationMapById.get(toId).getNameKey() : null;
                    if (fNameKey != null && tNameKey != null && c.getSourceKey() != null && c.getDestKey() != null) {
                        if ((c.getSourceKey().equalsIgnoreCase(fNameKey) && c.getDestKey().equalsIgnoreCase(tNameKey)) ||
                                (c.getSourceKey().equalsIgnoreCase(tNameKey) && c.getDestKey().equalsIgnoreCase(fNameKey))) {
                            total += c.getDistanceMeters();
                            break;
                        }
                    }
                }
            }
        }

        return total;
    }

    // ----------------- MAP DRAWING -----------------
    private void drawAllMarkers() {
        if (mMap == null) return;

        mMap.clear();

        for (LocationModel loc : locationList) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title(loc.getName()));
        }

        // Optionally move camera to first location
        if (!locationList.isEmpty() && mMap != null) {
            LocationModel first = locationList.get(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(first.getLatitude(), first.getLongitude()), 16f));
        }
    }

    private void drawPathOnMap(List<LocationModel> path, double totalDistanceMeters) {
        if (mMap == null || path == null || path.isEmpty()) return;

        PolylineOptions opts = new PolylineOptions()
                .width(10f)
                .color(0xFF42A5F5)
                .geodesic(true);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        // track midpoint coordinates (average) in case we need marker placement
        double latSum = 0.0;
        double lngSum = 0.0;
        int count = 0;

        for (LocationModel l : path) {
            LatLng pt = new LatLng(l.getLatitude(), l.getLongitude());
            opts.add(pt);
            boundsBuilder.include(pt);

            latSum += l.getLatitude();
            lngSum += l.getLongitude();
            count++;
        }

        if (currentPolyline != null) currentPolyline.remove();
        currentPolyline = mMap.addPolyline(opts);

        // Remove old distance marker if exists
        if (distanceMarker != null) {
            distanceMarker.remove();
            distanceMarker = null;
        }

        // Create new distance marker at midpoint (average lat/lng)
        LatLng markerPos;
        if (count > 0) {
            markerPos = new LatLng(latSum / count, lngSum / count);
        } else {
            // fallback to first point
            LocationModel first = path.get(0);
            markerPos = new LatLng(first.getLatitude(), first.getLongitude());
        }

        String distanceText = String.format("%.2f m", totalDistanceMeters);
        distanceMarker = mMap.addMarker(new MarkerOptions()
                .position(markerPos)
                .title("Route distance")
                .snippet(distanceText)
                .anchor(0.5f, 0.5f)
        );

        if (distanceMarker != null) {
            distanceMarker.showInfoWindow(); // make the info window visible and persistent
        }

        // Move/zoom camera to include full path
        try {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 150; // offset from edges (in px)
            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, padding)
            );
        } catch (IllegalStateException e) {
            // boundsBuilder.build() throws if no points were added; fallback to zoom on first point
            if (!path.isEmpty()) {
                LocationModel first = path.get(0);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(first.getLatitude(), first.getLongitude()), 17f));
            }
        }

        btnStartNavigation.setVisibility(View.VISIBLE);  // show after route drawn

    }

    // ----------------- LOADING DIALOG -----------------
    private AlertDialog createLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.BlackBlueDialogTheme);
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        builder.setView(v);
        builder.setCancelable(false);
        return builder.create();
    }

    private void showLoading(String text) {
        if (loadingDialog == null) return;
        TextView tv = loadingDialog.findViewById(R.id.dialog_loading_text);
        if (tv != null) tv.setText(text);
        if (!loadingDialog.isShowing()) loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] grantResults) {
        if (requestCode == REQ_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        }
        super.onRequestPermissionsResult(requestCode, perms, grantResults);
    }

    private void startLiveNavigation() {

        LocationRequest request = LocationRequest.create()
                .setInterval(2000)            // update every 2 seconds
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        fusedLocation.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {

                Location loc = result.getLastLocation();
                if (loc == null) return;

                LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());

                // Update marker OR create
                if (userMarker == null) {
                    userMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(pos)
                                    .title("You are here")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    );
                } else {
                    userMarker.setPosition(pos);
                }

                // Optionally keep camera following the user
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18));

            }
        }, getMainLooper());
    }


}
