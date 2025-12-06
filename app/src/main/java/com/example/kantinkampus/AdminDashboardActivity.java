package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvAdminName, tvTotalOrders, tvPendingOrders, tvTotalRevenue, tvTotalCustomers;
    private CardView btnManageOrders, btnManageStands, btnManageMenus, btnViewCustomers, btnLogout;

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        // Check if user is admin
        if (!sessionManager.isAdmin()) {
            // Redirect to customer home if not admin
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        tvAdminName = findViewById(R.id.tvAdminName);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalCustomers = findViewById(R.id.tvTotalCustomers);

        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnManageStands = findViewById(R.id.btnManageStands);
        btnManageMenus = findViewById(R.id.btnManageMenus);
        btnViewCustomers = findViewById(R.id.btnViewCustomers);
        btnLogout = findViewById(R.id.btnLogout);

        // Set admin name
        User admin = sessionManager.getUserDetails();
        if (admin != null) {
            tvAdminName.setText("Selamat datang, " + admin.getName());
        }

        // Load statistics
        loadStatistics();

        // Set click listeners
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload statistics when returning to dashboard
        loadStatistics();
    }

    private void loadStatistics() {
        // Get statistics from database
        int totalOrders = dbHelper.getTotalOrdersCount();
        int pendingOrders = dbHelper.getPendingOrdersCount();
        int totalRevenue = dbHelper.getTotalRevenue();
        int totalCustomers = dbHelper.getTotalCustomersCount();

        // Update UI
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvPendingOrders.setText(String.valueOf(pendingOrders));
        tvTotalCustomers.setText(String.valueOf(totalCustomers));

        // Format currency
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvTotalRevenue.setText(formatter.format(totalRevenue));
    }

    private void setupClickListeners() {


        btnManageStands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ManageStandsActivity.class);
                startActivity(intent);
            }
        });



        btnViewCustomers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, ViewCustomersActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmation();
            }
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Logout
                    sessionManager.logoutUser();

                    // Redirect to login
                    Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        // Show logout confirmation when back is pressed
        showLogoutConfirmation();
    }
}