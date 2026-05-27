package com.example.campusnavigator.firebase.repository;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.example.campusnavigator.firebase.constants.FirebaseConstants;
import com.example.campusnavigator.firebase.model.LocationModel;
import com.example.campusnavigator.firebase.util.FirestoreCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FirestoreRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("location_images");

    /**
     * Adds a new location to Firestore with duplicate name check.
     * Creates a lowercase nameKey to ensure uniqueness even if case differs.
     */
    public void addLocation(LocationModel location, Uri imageUri, FirestoreCallback callback) {
        if (location.getName() == null || location.getName().trim().isEmpty()) {
            callback.onFailure("Location name cannot be empty");
            return;
        }

        // Create nameKey for uniqueness
        String nameKey = location.getName().trim().toLowerCase();
        location.setNameKey(nameKey);

        // Check if location with same nameKey already exists
        firestore.collection(FirebaseConstants.COLLECTION_LOCATIONS)
                .whereEqualTo("nameKey", nameKey)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        callback.onFailure("A location with this name already exists");
                    } else {
                        // No duplicate — proceed with adding location
                        String documentId = firestore.collection(FirebaseConstants.COLLECTION_LOCATIONS).document().getId();
                        location.setId(documentId);

                        firestore.collection(FirebaseConstants.COLLECTION_LOCATIONS)
                                .document(documentId)
                                .set(location)
                                .addOnSuccessListener(aVoid -> callback.onSuccess("Location added successfully"))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Fetch all locations from Firestore
     */
    public void getAllLocations(FirestoreCallback callback) {
        firestore.collection(FirebaseConstants.COLLECTION_LOCATIONS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<LocationModel> list = new ArrayList<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        LocationModel location = doc.toObject(LocationModel.class);
                        if (location != null) list.add(location);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Delete a location by ID
     */
    public void deleteLocation(String id, FirestoreCallback callback) {
        firestore.collection(FirebaseConstants.COLLECTION_LOCATIONS)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess("Deleted successfully"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
