package com.example.campusnavigator.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.admin.adapter.LocationAdapter;
import com.example.campusnavigator.firebase.model.LocationModel;
import com.example.campusnavigator.firebase.repository.FirestoreRepository;
import com.example.campusnavigator.firebase.util.FirestoreCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationsActivity extends AppCompatActivity {

    private RecyclerView rvLocations;
    private LocationAdapter adapter;
    private List<LocationModel> locationList;
    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_locations);

        // Initialize RecyclerView
        rvLocations = findViewById(R.id.rvLocations);
        rvLocations.setLayoutManager(new LinearLayoutManager(this));
        rvLocations.setHasFixedSize(true);

        // Initialize list and adapter
        locationList = new ArrayList<>();
        adapter = new LocationAdapter(this, locationList); // ✅ Pass context here
        rvLocations.setAdapter(adapter);

        // Initialize Firestore repository
        repository = new FirestoreRepository();

        // Fetch locations from Firestore
        fetchLocations();
    }

    private void fetchLocations() {
        repository.getAllLocations(new FirestoreCallback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof List) {
                    locationList.clear();
                    locationList.addAll((List<LocationModel>) data);
                    adapter.notifyDataSetChanged();

                    if (locationList.isEmpty()) {
                        Toast.makeText(ViewLocationsActivity.this, "No locations found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onSuccess(String message) {
                Toast.makeText(ViewLocationsActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ViewLocationsActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e("ViewLocations", "Firestore error: " + errorMessage);
            }
        });
    }
}
