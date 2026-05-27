package com.example.campusnavigator.admin;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.campusnavigator.R;

public class AdminLoginActivity extends AppCompatActivity {

    EditText etAdminId, etPassword;
    AppCompatButton btnLogin;
    ImageView eyeIcon;      // 👁️ Show/Hide button
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);

        // Initialize views
        etAdminId = findViewById(R.id.etAdminId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        eyeIcon = findViewById(R.id.eyeIcon);  // 👁️ eye icon view

        // Toggle password visibility
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        // Login button click
        btnLogin.setOnClickListener(v -> {
            String adminId = etAdminId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (adminId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (adminId.equals("ADMIN") && password.equals("ADMIN")) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid ID or Password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            eyeIcon.setImageResource(R.drawable.ic_eye); // closed eye icon
        } else {
            // Show Password
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            eyeIcon.setImageResource(R.drawable.eye_closed); // open eye icon
        }
        isPasswordVisible = !isPasswordVisible;

        // Move cursor to end
        etPassword.setSelection(etPassword.length());
    }
}
