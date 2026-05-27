package com.example.campusnavigator.admin.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.firebase.model.ConnectionModel;
import com.example.campusnavigator.firebase.repository.ConnectionsRepository;

import java.util.List;

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.VH> {

    private final Context context;
    private final List<ConnectionModel> list;
    private final ConnectionsRepository repository;

    public ConnectionAdapter(Context context, List<ConnectionModel> list) {
        this.context = context;
        this.list = list;
        this.repository = new ConnectionsRepository();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_connection_xml, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ConnectionModel c = list.get(position);
        holder.tvSource.setText(c.getSourceName());
        holder.tvDest.setText(c.getDestinationName());
        holder.tvDistance.setText(String.format("%.1f m", c.getDistanceMeters()));

        holder.btnDelete.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.BlackBlueDialogTheme)
                    .setTitle("Delete Connection")
                    .setMessage("Delete connection from\n" + c.getSourceName() + " → " + c.getDestinationName() + "?")
                    .setPositiveButton("Delete", (d, w) -> {
                        // Delete connection using sourceId and destId (Firestore IDs)
                        String sourceId = c.getSourceId();
                        String destId = c.getDestId();

                        if (sourceId == null || destId == null) {
                            Toast.makeText(context, "Cannot delete: missing connection IDs", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        repository.deleteConnection(sourceId, destId, new ConnectionsRepository.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                int idx = holder.getAdapterPosition();
                                if (idx >= 0 && idx < list.size()) {
                                    list.remove(idx);
                                    notifyItemRemoved(idx);
                                    notifyItemRangeChanged(idx, list.size());
                                }
                                Toast.makeText(context, "Connection deleted", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                    .create();

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvSource, tvDest, tvDistance;
        ImageView btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvSource = itemView.findViewById(R.id.manageConnectionsItem_source);
            tvDest = itemView.findViewById(R.id.manageConnectionsItem_destination);
            tvDistance = itemView.findViewById(R.id.manageConnectionsItem_distance);
            btnDelete = itemView.findViewById(R.id.manageConnectionsItem_delete);
        }
    }
}
