package com.example.campusnavigator.admin;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.admin.adapter.ConnectionAdapter;
import com.example.campusnavigator.firebase.model.ConnectionModel;
import com.example.campusnavigator.firebase.model.LocationModel;
import com.example.campusnavigator.firebase.repository.ConnectionsRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageConnectionsActivity extends AppCompatActivity {

    private Spinner spinnerSource, spinnerDestination;
    private Button btnAddConnection;
    private RecyclerView rvConnections;

    private final List<LocationModel> locations = new ArrayList<>();
    private final Map<String, LocationModel> locationById = new HashMap<>();

    private final List<ConnectionModel> connections = new ArrayList<>();
    private ConnectionAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ConnectionsRepository repository = new ConnectionsRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.activity_manage_connections);
        MaterialToolbar toolbar = findViewById(R.id.manageConnections_toolbar);

// Set toolbar as action bar
        setSupportActionBar(toolbar);

// Set title again (just to be sure)
        getSupportActionBar().setTitle("Manage Connections");

// Make title color white using Java
        toolbar.setTitleTextColor(getColor(android.R.color.white));

// Enable back arrow click
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();   // or finish();
        });


        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );


        spinnerSource = findViewById(R.id.manageConnections_sourceSpinner);
        spinnerDestination = findViewById(R.id.manageConnections_destinationSpinner);
        btnAddConnection = findViewById(R.id.manageConnections_addButton);
        rvConnections = findViewById(R.id.manageConnections_recyclerView);

        adapter = new ConnectionAdapter(this, connections);
        rvConnections.setLayoutManager(new LinearLayoutManager(this));
        rvConnections.setAdapter(adapter);

        fetchLocations();
        fetchConnections();

        btnAddConnection.setOnClickListener(v -> addConnection());
    }

    private void fetchLocations() {
        db.collection("locations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    locations.clear();
                    locationById.clear();

                    List<String> names = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        String id = doc.getId();
                        String name = doc.getString("name");
                        double lat = doc.getDouble("latitude");
                        double lng = doc.getDouble("longitude");

                        if (name == null) continue;

                        LocationModel loc = new LocationModel(id, name, lat, lng);

                        locations.add(loc);
                        locationById.put(id, loc);
                        names.add(name);
                    }

                    ArrayAdapter<String> adapterSpinner =
                            new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names){
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);

                                    TextView tv = view.findViewById(android.R.id.text1);
                                    tv.setTextColor(getColor(android.R.color.darker_gray));   // <-- CHANGE COLOR HERE

                                    return view;
                                }

                                @Override
                                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getDropDownView(position, convertView, parent);

                                    TextView tv = view.findViewById(android.R.id.text1);
                                    tv.setTextColor(getColor(android.R.color.darker_gray));   // <-- CHANGE COLOR HERE

                                    return view;
                                }
                            };

                    adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinnerSource.setAdapter(adapterSpinner);
                    spinnerDestination.setAdapter(adapterSpinner);
                });
    }

    private void fetchConnections() {
        repository.getConnections(new ConnectionsRepository.DataCallback() {
            @Override
            public void onSuccess(List<ConnectionModel> list) {
                connections.clear();
                connections.addAll(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageConnectionsActivity.this,
                        "Failed to load connections: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addConnection() {

        int srcPos = spinnerSource.getSelectedItemPosition();
        int dstPos = spinnerDestination.getSelectedItemPosition();

        if (srcPos == dstPos) {
            Toast.makeText(this, "Source and destination cannot be same", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationModel src = locations.get(srcPos);
        LocationModel dst = locations.get(dstPos);

        String sourceId = src.getId();
        String destId = dst.getId();

        // Check duplicates via IDs
        db.collection("connections")
                .whereEqualTo("sourceId", sourceId)
                .whereEqualTo("destId", destId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.isEmpty()) {
                        Toast.makeText(this, "Connection already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("connections")
                            .whereEqualTo("sourceId", destId)
                            .whereEqualTo("destId", sourceId)
                            .get()
                            .addOnSuccessListener(reverse -> {

                                if (!reverse.isEmpty()) {
                                    Toast.makeText(this, "Reverse connection already exists", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                createConnection(src, dst);
                            });
                });
    }

    private void createConnection(LocationModel src, LocationModel dst) {

        float[] result = new float[1];
        android.location.Location.distanceBetween(
                src.getLatitude(), src.getLongitude(),
                dst.getLatitude(), dst.getLongitude(),
                result
        );

        double distanceMeters = result[0];

        ConnectionModel newConnection = new ConnectionModel(
                src.getName(),
                dst.getName(),
                src.getId(),     // 🔥 Firestore ID
                dst.getId(),     // 🔥 Firestore ID
                distanceMeters
        );

        repository.addConnection(newConnection, new ConnectionsRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                connections.add(newConnection);
                adapter.notifyItemInserted(connections.size() - 1);
                Toast.makeText(ManageConnectionsActivity.this,
                        "Added: " + String.format("%.2f m", distanceMeters),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageConnectionsActivity.this,
                        "Failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
