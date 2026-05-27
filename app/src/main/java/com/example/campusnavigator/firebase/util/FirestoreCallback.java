package com.example.campusnavigator.firebase.util;

public interface FirestoreCallback {
    void onSuccess(Object data);

    void onSuccess(String message);
    void onFailure(String errorMessage);
}
