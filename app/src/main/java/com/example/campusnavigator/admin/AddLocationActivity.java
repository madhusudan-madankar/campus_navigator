package com.example.campusnavigator.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusnavigator.R;
import com.example.campusnavigator.firebase.model.LocationModel;
import com.example.campusnavigator.firebase.repository.FirestoreRepository;
import com.example.campusnavigator.firebase.util.FirestoreCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddLocationActivity extends AppCompatActivity {

    EditText etName, etLat, etLng, etDesc;
    ImageView imgPreview;
    Uri selectedImageUri;

    // Launchers
    ActivityResultLauncher<Intent> mapLauncher;
    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;

    // --- Camera launcher ---
    ActivityResultLauncher<Uri> takePictureLauncher;
    AlertDialog progressDialog;
    Uri photoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        etName = findViewById(R.id.etLocationName);
        etLat = findViewById(R.id.etLatitude);
        etLng = findViewById(R.id.etLongitude);
        etDesc = findViewById(R.id.etDescription);
        imgPreview = findViewById(R.id.imgPreview);

        // Register camera launcher (returns boolean if success)
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (isSuccess && photoUri != null) {
                        selectedImageUri = photoUri;
                        imgPreview.setImageURI(photoUri);
                    } else {
                        Toast.makeText(this, "Camera image not found", Toast.LENGTH_SHORT).show();
                    }
                });

        // Map Picker
        mapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            double lat = data.getDoubleExtra("latitude", 0);
                            double lng = data.getDoubleExtra("longitude", 0);
                            etLat.setText(String.valueOf(lat));
                            etLng.setText(String.valueOf(lng));
                        }
                    }
                });

        findViewById(R.id.btnPickFromMap).setOnClickListener(v -> {
            Intent intent = new Intent(AddLocationActivity.this, MapPickerActivity.class);
            mapLauncher.launch(intent);
        });

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imgPreview.setImageURI(uri);
                    }
                });

        findViewById(R.id.btnSelectImage).setOnClickListener(v -> showImagePickerDialog());
        findViewById(R.id.btnSave).setOnClickListener(v -> validateFields());
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialogue_progress, null);
            progressDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .create();
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    private void showImagePickerDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_picker, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button btnCamera = dialogView.findViewById(R.id.btnCamera);
        Button btnGallery = dialogView.findViewById(R.id.btnGallery);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnCamera.setOnClickListener(v -> {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.CAMERA},
                        101
                );
            } else {
                openCamera();
            }
            dialog.dismiss();
        });

        btnGallery.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private Uri createImageUri() {
        String imageName = "IMG_" + System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void openCamera() {
        photoUri = createImageUri();
        if (photoUri != null) {
            takePictureLauncher.launch(photoUri);
        } else {
            Toast.makeText(this, "Unable to create file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String uriToBase64(Uri imageUri) {
        if (imageUri == null) return "";

        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); // compress to 80% quality
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void validateFields() {
        String name = etName.getText().toString().trim();
        String latStr = etLat.getText().toString().trim();
        String lngStr = etLng.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = Double.parseDouble(latStr);
        double longitude = Double.parseDouble(lngStr);
        String appUniqueId = getPackageName();
        long timestamp = System.currentTimeMillis();

        String imageBase64 = uriToBase64(selectedImageUri); // convert image to Base64

        LocationModel location = new LocationModel(
                "", name, latitude, longitude, desc, imageBase64, appUniqueId, timestamp
        );

        FirestoreRepository repository = new FirestoreRepository();

        // SHOW progress dialog
        showProgressDialog();

        repository.addLocation(location, null, new FirestoreCallback() {
            @Override
            public void onSuccess(Object data) {
                hideProgressDialog();
            }

            @Override
            public void onSuccess(String message) {
                hideProgressDialog();
                Toast.makeText(AddLocationActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();

                // Clear fields
                etName.setText("");
                etLat.setText("");
                etLng.setText("");
                etDesc.setText("");
                imgPreview.setImageResource(R.drawable.black_blue_camera); // placeholder
                selectedImageUri = null;
            }

            @Override
            public void onFailure(String errorMessage) {
                hideProgressDialog();
                Toast.makeText(AddLocationActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
