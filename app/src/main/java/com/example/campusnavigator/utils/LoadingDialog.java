package com.example.campusnavigator.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.campusnavigator.R;

public class LoadingDialog {

    private AlertDialog dialog;
    private TextView loadingText;

    public LoadingDialog(Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.BlackBlueDialogTheme);

        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null);
        loadingText = view.findViewById(R.id.dialog_loading_text);
        loadingText.setText(message);

        builder.setView(view);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // transparent background
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setMessage(String message) {
        if (loadingText != null) {
            loadingText.setText(message);
        }
    }
}
