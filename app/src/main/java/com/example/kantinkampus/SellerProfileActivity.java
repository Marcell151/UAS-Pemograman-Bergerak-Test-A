package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

/**
 * SELLER PROFILE ACTIVITY
 * Shows seller profile information and settings
 */
public class SellerProfileActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvPhone, tvIdNumber, tvRole;
    private Button btnLogout, btnChangePassword;
    private CardView cardProfile, cardAccount, cardAbout;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        // Initialize
        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profil Penjual");
        }

        // Initialize views
        initViews();

        // Load profile data
        loadProfileData();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvIdNumber = findViewById(R.id.tvIdNumber);
        tvRole = findViewById(R.id.tvRole);
        btnLogout = findViewById(R.id.btnLogout);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        cardProfile = findViewById(R.id.cardProfile);
        cardAccount = findViewById(R.id.cardAccount);
        cardAbout = findViewById(R.id.cardAbout);
    }

    private void loadProfileData() {
        User user = sessionManager.getUserDetails();

        if (user != null) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
            tvPhone.setText(user.getPhone());
            tvIdNumber.setText(user.getIdNumberLabel());
            tvRole.setText(user.getRoleDisplay());
        }
    }

    private void setupListeners() {
        // Change password (placeholder)
        btnChangePassword.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Ubah Password")
                    .setMessage("Fitur ubah password akan segera tersedia")
                    .setPositiveButton("OK", null)
                    .show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Yakin ingin keluar dari aplikasi?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        sessionManager.logoutUser();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // About card
        cardAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Tentang Aplikasi")
                    .setMessage("Kantin KampusKu v3.0\n\n" +
                            "Aplikasi pemesanan makanan kampus\n\n" +
                            "Fitur:\n" +
                            "• Dual Role System (Penjual & Pembeli)\n" +
                            "• Multi-Stand Orders\n" +
                            "• Payment Verification\n" +
                            "• Real-time Order Tracking\n" +
                            "• Review & Rating System\n\n" +
                            "Dibuat dengan ❤️")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}