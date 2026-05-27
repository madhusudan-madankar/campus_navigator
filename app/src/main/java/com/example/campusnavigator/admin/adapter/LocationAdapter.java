package com.example.campusnavigator.admin.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.firebase.model.LocationModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private Context context;
    private List<LocationModel> locationList;
    private FirebaseFirestore firestore;

    public LocationAdapter(Context context, List<LocationModel> locationList) {
        this.context = context;
        this.locationList = locationList;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_locations, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationModel location = locationList.get(position);

        holder.tvName.setText(location.getName());
        holder.tvId.setText("ID: " + location.getId());
        holder.tvCoordinates.setText("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());

        // Convert Base64 image to Bitmap
        if (location.getImageBase64() != null && !location.getImageBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(location.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imgLocation.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imgLocation.setImageResource(R.drawable.vit_logo);
            }
        } else {
            holder.imgLocation.setImageResource(R.drawable.vit_logo);
        }

        // Show full info dialog when clicking the card
        holder.itemView.setOnClickListener(v -> showDetailsDialog(location));

        // Delete confirmation when clicking delete icon
        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(position, location));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId, tvCoordinates;
        ImageView imgLocation, btnDelete;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvLocationName);
            tvId = itemView.findViewById(R.id.tvLocationId);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            imgLocation = itemView.findViewById(R.id.imgLocation);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // --- Full info dialog ---
    private void showDetailsDialog(LocationModel location) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_location_details, null);

        ImageView imgFull = dialogView.findViewById(R.id.imgFull);
        TextView tvName = dialogView.findViewById(R.id.tvName);
        TextView tvId = dialogView.findViewById(R.id.tvId);
        TextView tvLatLng = dialogView.findViewById(R.id.tvLatLng);
        TextView tvDesc = dialogView.findViewById(R.id.tvDesc);

        tvName.setText(location.getName());
        tvId.setText("ID: " + location.getId());
        tvLatLng.setText("Lat: " + location.getLatitude() + "\nLng: " + location.getLongitude());
        tvDesc.setText(location.getDescription());

        if (location.getImageBase64() != null && !location.getImageBase64().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(location.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgFull.setImageBitmap(bitmap);
            } catch (Exception e) {
                imgFull.setImageResource(R.drawable.vit_logo);
            }
        } else {
            imgFull.setImageResource(R.drawable.vit_logo);
        }

        new AlertDialog.Builder(context, R.style.BlackBlueDialogTheme)
                .setView(dialogView)
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .create()
                .show();
    }

    // --- Delete confirmation dialog ---
    private void showDeleteDialog(int position, LocationModel location) {
        new AlertDialog.Builder(context, R.style.BlackBlueDialogTheme)
                .setTitle("Delete Location")
                .setMessage("Are you sure you want to delete \"" + location.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteLocation(position, location))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // --- Delete from Firestore + remove from list instantly ---
    private void deleteLocation(int position, LocationModel location) {
        firestore.collection("locations")
                .document(location.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    locationList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Location deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
