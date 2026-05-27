package com.example.campusnavigator.firebase.model;

public class ConnectionModel {

    // Use explicit Firestore ID fields for graph (sourceId/destId).
    // Keep legacy getters getSourceKey()/getDestKey() for compatibility.
    private String id;
    private String sourceName;
    private String destinationName;
    private String sourceId;   // Firestore ID of source location
    private String destId;     // Firestore ID of destination location
    private double distance;
    private String pathType;
    private long timestamp;

    // Required empty constructor for Firebase
    public ConnectionModel() { }

    // Basic constructor for adding new connection (used in ManageConnectionsActivity)
    public ConnectionModel(String sourceName,
                           String destinationName,
                           String sourceId,
                           String destId,
                           double distance) {
        this.sourceName = sourceName;
        this.destinationName = destinationName;
        this.sourceId = sourceId;
        this.destId = destId;
        this.distance = distance;
        this.pathType = "normal";  // default
        this.timestamp = System.currentTimeMillis();
    }

    // Full constructor (pathType also provided)
    public ConnectionModel(String sourceName,
                           String destinationName,
                           String sourceId,
                           String destId,
                           double distance,
                           String pathType) {
        this.sourceName = sourceName;
        this.destinationName = destinationName;
        this.sourceId = sourceId;
        this.destId = destId;
        this.distance = distance;
        this.pathType = pathType;
        this.timestamp = System.currentTimeMillis();
    }

    public ConnectionModel(String id,
                           String sourceName,
                           String destinationName,
                           String sourceId,
                           String destId,
                           double distance) {
        this.id = id;
        this.sourceName = sourceName;
        this.destinationName = destinationName;
        this.sourceId = sourceId;
        this.destId = destId;
        this.distance = distance;
        this.pathType = "normal";
        this.timestamp = System.currentTimeMillis();
    }

    // Optional constructor with all fields
    public ConnectionModel(String id,
                           String sourceName,
                           String destinationName,
                           String sourceId,
                           String destId,
                           double distance,
                           String pathType) {
        this.id = id;
        this.sourceName = sourceName;
        this.destinationName = destinationName;
        this.sourceId = sourceId;
        this.destId = destId;
        this.distance = distance;
        this.pathType = pathType;
        this.timestamp = System.currentTimeMillis();
    }

    // --- Getters & Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    // New canonical getters for Firestore IDs
    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    // Backwards-compatible aliases (some code might still call getSourceKey/getDestKey)
    public String getSourceKey() {
        return sourceId;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceId = sourceKey;
    }

    public String getDestKey() {
        return destId;
    }

    public void setDestKey(String destKey) {
        this.destId = destKey;
    }

    public double getDistanceMeters() {
        return distance;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distance = distanceMeters;
    }

    public String getPathType() {
        return pathType;
    }

    public void setPathType(String pathType) {
        this.pathType = pathType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // For displaying in list (ManageConnectionsActivity)
    public String getDisplayText() {
        // Example: "Library → Canteen (Distance: 120.5 m)"
        return sourceName + " → " + destinationName + "  (" + String.format("%.1f", distance) + " m)";
    }
}
