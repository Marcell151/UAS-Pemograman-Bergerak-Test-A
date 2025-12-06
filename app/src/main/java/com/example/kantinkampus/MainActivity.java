package com.example.kantinkampus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {
    private CardView btnPilihStand, btnKeranjang, btnRiwayat, btnTentang, cardCart;
    private TextView tvCartBadge, tvWelcomeMessage;
    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private int userId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Check if user is admin (redirect to admin dashboard)
        if (sessionManager.isAdmin()) {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Get user ID
        userId = sessionManager.getUserId();

        // Initialize views
        btnPilihStand = findViewById(R.id.btnPilihStand);
        btnKeranjang = findViewById(R.id.btnKeranjang);
        btnRiwayat = findViewById(R.id.btnRiwayat);
        btnTentang = findViewById(R.id.btnTentang);
        cardCart = findViewById(R.id.cardCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);

        // Set welcome message
        User user = sessionManager.getUserDetails();
        if (user != null) {
            String greeting = "Halo, " + user.getName() + "! 👋";
            tvWelcomeMessage.setText(greeting);
        }

        // Setup click listeners
        btnPilihStand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StandListActivity.class);
                startActivity(intent);
            }
        });

        btnKeranjang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        cardCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        btnRiwayat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OrderHistoryActivity.class);
                startActivity(intent);
            }
        });

        btnTentang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        // Logout button
        findViewById(R.id.btnLogoutCustomer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmation();
            }
        });

        // Optional: Add favorites button if you have it in layout
        // findViewById(R.id.btnFavorites).setOnClickListener(v -> {
        //     Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
        //     startActivity(intent);
        // });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    private void updateCartBadge() {
        int count = dbHelper.getCartCount(userId);
        if (count > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(String.valueOf(count));
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Keluar Aplikasi")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    finishAffinity();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}