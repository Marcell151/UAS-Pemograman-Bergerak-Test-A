package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * SELLER DASHBOARD - Main Hub for Sellers
 * Features:
 * - View stand info
 * - Manage menus
 * - Manage orders (with payment verification)
 * - View statistics
 */
public class SellerDashboardActivity extends AppCompatActivity {
    private static final String TAG = "SellerDashboard";

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private TextView tvWelcome, tvStandName, tvTotalOrders, tvPendingPayments,
            tvTodayRevenue, tvTotalMenus;
    private CardView cardMyStand, cardManageMenus, cardManageOrders,
            cardStatistics, cardNotifications;

    private int sellerId;
    private Stand myStand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);
        sellerId = sessionManager.getUserId();

        // Check if seller is logged in
        if (!sessionManager.isSeller()) {
            Toast.makeText(this, "Akses ditolak! Anda bukan seller.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Load data
        loadSellerData();
        loadStatistics();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStandName = findViewById(R.id.tvStandName);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingPayments = findViewById(R.id.tvPendingPayments);
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue);
        tvTotalMenus = findViewById(R.id.tvTotalMenus);

        cardMyStand = findViewById(R.id.cardMyStand);
        cardManageMenus = findViewById(R.id.cardManageMenus);
        cardManageOrders = findViewById(R.id.cardManageOrders);
        cardStatistics = findViewById(R.id.cardStatistics);
        cardNotifications = findViewById(R.id.cardNotifications);

        // Set welcome message
        tvWelcome.setText(sessionManager.getGreetingMessage());
    }

    private void loadSellerData() {
        myStand = dbHelper.getStandBySeller(sellerId);

        if (myStand != null) {
            tvStandName.setText("Stand: " + myStand.getNama());
        } else {
            tvStandName.setText("⚠️ Anda belum memiliki stand");
        }
    }

    private void loadStatistics() {
        try {
            if (myStand == null) {
                tvTotalOrders.setText("0");
                tvPendingPayments.setText("0");
                tvTodayRevenue.setText("Rp 0");
                tvTotalMenus.setText("0");
                return;
            }

            // Total orders
            int totalOrders = dbHelper.getTotalOrdersBySeller(sellerId, "all");
            tvTotalOrders.setText(String.valueOf(totalOrders));

            // Pending payments
            int pendingPayments = dbHelper.getTotalOrdersBySeller(sellerId, "pending_verification");
            tvPendingPayments.setText(String.valueOf(pendingPayments));

            // Today revenue
            int revenue = dbHelper.getTotalRevenue(sellerId);
            tvTodayRevenue.setText("Rp " + formatPrice(revenue));

            // Total menus
            int totalMenus = dbHelper.getMenusByStand(myStand.getId()).size();
            tvTotalMenus.setText(String.valueOf(totalMenus));

        } catch (Exception e) {
            Log.e(TAG, "Error loading statistics: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        cardMyStand.setOnClickListener(v -> openMyStand());
        cardManageMenus.setOnClickListener(v -> openManageMenus());
        cardManageOrders.setOnClickListener(v -> openManageOrders());
        cardStatistics.setOnClickListener(v -> openStatistics());
        cardNotifications.setOnClickListener(v -> openNotifications());
    }

    private void openMyStand() {
        if (myStand == null) {
            // Show dialog to create stand
            showCreateStandDialog();
        } else {
            // Show stand details
            Intent intent = new Intent(this, MyStandActivity.class);
            startActivity(intent);
        }
    }

    private void openManageMenus() {
        if (myStand == null) {
            Toast.makeText(this, "⚠️ Buat stand terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SellerManageMenusActivity.class);
        intent.putExtra("stand_id", myStand.getId());
        startActivity(intent);
    }

    private void openManageOrders() {
        if (myStand == null) {
            Toast.makeText(this, "⚠️ Buat stand terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SellerManageOrdersActivity.class);
        startActivity(intent);
    }

    private void openStatistics() {
        if (myStand == null) {
            Toast.makeText(this, "⚠️ Buat stand terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SellerStatisticsActivity.class);
        startActivity(intent);
    }

    private void openNotifications() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    private void showCreateStandDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🏪 Buat Stand Baru");
        builder.setMessage("Anda belum memiliki stand. Buat stand sekarang untuk mulai berjualan!");

        builder.setPositiveButton("Buat Stand", (dialog, which) -> {
            Intent intent = new Intent(this, CreateStandActivity.class);
            startActivity(intent);
        });

        builder.setNegativeButton("Nanti", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.seller_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            // Open profile
            Intent intent = new Intent(this, SellerProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            // Logout
            showLogoutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Intent intent = new Intent(SellerDashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private String formatPrice(int price) {
        return String.format("%,d", price).replace(',', '.');
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadSellerData();
        loadStatistics();
    }

    @Override
    public void onBackPressed() {
        // Show exit confirmation
        new AlertDialog.Builder(this)
                .setTitle("Keluar Aplikasi")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    moveTaskToBack(true);
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}