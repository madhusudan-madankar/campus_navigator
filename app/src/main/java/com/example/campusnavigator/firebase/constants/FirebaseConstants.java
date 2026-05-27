package com.example.campusnavigator.firebase.constants;

public class FirebaseConstants {

    // Firestore collection names
    public static final String COLLECTION_LOCATIONS = "locations";
    public static final String COLLECTION_CONNECTIONS = "connections";

    // Location fields
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_LATITUDE = "latitude";
    public static final String FIELD_LONGITUDE = "longitude";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_IMAGE_BASE64 = "imageBase64";
    public static final String FIELD_APP_UNIQUE_ID = "appUniqueId";
    public static final String FIELD_TIMESTAMP = "timestamp";

    // Connection fields
    public static final String FIELD_SOURCE_ID = "sourceId";
    public static final String FIELD_SOURCE_NAME = "sourceName";
    public static final String FIELD_SOURCE_LAT = "sourceLatitude";
    public static final String FIELD_SOURCE_LNG = "sourceLongitude";

    public static final String FIELD_DEST_ID = "destId";
    public static final String FIELD_DEST_NAME = "destName";
    public static final String FIELD_DEST_LAT = "destLatitude";
    public static final String FIELD_DEST_LNG = "destLongitude";

    public static final String FIELD_DISTANCE = "distance";
}
