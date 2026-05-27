package com.example.campusnavigator.firebase.model;

public class LocationModel {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private String imageBase64;
    private String appUniqueId;
    private long timestamp;
    private String nameKey;

    // Default constructor (Firestore requires it)
    public LocationModel() { }

    // Full constructor
    public LocationModel(String id, String name, double latitude, double longitude,
                         String description, String imageBase64, String appUniqueId, long timestamp) {

        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.imageBase64 = imageBase64;
        this.appUniqueId = appUniqueId;
        this.timestamp = timestamp;

        // AUTO GENERATE NAME KEY
        if (name != null) {
            this.nameKey = name.toLowerCase().trim();
        } else {
            this.nameKey = "";
        }
    }

    // Minimal constructor (for spinner or simple use)
    public LocationModel(String id, String name, double latitude, double longitude) {

        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = "";
        this.imageBase64 = "";
        this.appUniqueId = "";
        this.timestamp = System.currentTimeMillis();

        // AUTO GENERATE NAME KEY
        if (name != null) {
            this.nameKey = name.toLowerCase().trim();
        } else {
            this.nameKey = "";
        }
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public String getImageBase64() { return imageBase64; }
    public String getAppUniqueId() { return appUniqueId; }
    public long getTimestamp() { return timestamp; }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) {
        this.name = name;
        if (name != null) {
            this.nameKey = name.toLowerCase().trim();   // Auto-generate
        }
    }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setDescription(String description) { this.description = description; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public void setAppUniqueId(String appUniqueId) { this.appUniqueId = appUniqueId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
