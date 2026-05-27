package com.example.campusnavigator.firebase.repository;

import android.util.Log;

import com.example.campusnavigator.firebase.model.ConnectionModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsRepository {

    private static final String COLLECTION_NAME = "connections";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // -------------------------------------------------------------
    // CALLBACKS
    // -------------------------------------------------------------
    public interface DataCallback {
        void onSuccess(List<ConnectionModel> connections);
        void onFailure(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // -------------------------------------------------------------
    // FETCH ALL CONNECTIONS
    // -------------------------------------------------------------
    public void getConnections(DataCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(query -> {
                    List<ConnectionModel> list = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : query) {
                        ConnectionModel c = doc.toObject(ConnectionModel.class);

                        // Ensure Firestore ID is injected into object
                        if (c != null && (c.getId() == null || c.getId().isEmpty())) {
                            c.setId(doc.getId());
                        }

                        list.add(c);
                    }

                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // -------------------------------------------------------------
    // ADD CONNECTION USING FIRESTORE IDs
    // -------------------------------------------------------------
    public void addConnection(ConnectionModel connection, ActionCallback callback) {

        // You must ensure:
        // connection.sourceId = Firestore Location document ID
        // connection.destId   = Firestore Location document ID

        CollectionReference ref = db.collection(COLLECTION_NAME);

        // Main connection
        ref.add(connection)
                .addOnSuccessListener(docRef -> {

                    String genId = docRef.getId();
                    connection.setId(genId);

                    ref.document(genId).set(connection);

                    Log.d("Firestore", "Connection added: " + genId);

                    // Create automatically reversed connection
                    ConnectionModel reverse = new ConnectionModel(
                            connection.getDestinationName(),
                            connection.getSourceName(),
                            connection.getDestId(),     // SWAP IDs
                            connection.getSourceId(),
                            connection.getDistanceMeters(),
                            connection.getPathType()
                    );

                    ref.add(reverse)
                            .addOnSuccessListener(docRef2 -> {
                                String revId = docRef2.getId();
                                reverse.setId(revId);
                                ref.document(revId).set(reverse);

                                Log.d("Firestore", "Reverse connection added: " + revId);

                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Failed to add reverse connection", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding connection", e);
                    callback.onFailure(e);
                });
    }

    // -------------------------------------------------------------
    // UPDATE CONNECTION
    // -------------------------------------------------------------
    public void updateConnection(String docId, ConnectionModel connection, ActionCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(docId)
                .set(connection)
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // -------------------------------------------------------------
    // DELETE CONNECTION USING **sourceId + destId**
    // -------------------------------------------------------------
    public void deleteConnection(String sourceId, String destId, ActionCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("sourceId", sourceId)
                .whereEqualTo("destId", destId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {

                        for (DocumentSnapshot doc : query.getDocuments()) {
                            doc.getReference().delete();
                        }

                        callback.onSuccess();
                    } else {
                        callback.onFailure(new Exception("No matching connection found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ConnectionsRepository", "Error deleting connection", e);
                    callback.onFailure(e);
                });
    }
}
