package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

/**
 * SELLER DASHBOARD ACTIVITY
 * Main dashboard for sellers with statistics and quick actions
 */
public class SellerDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvTotalOrders, tvTotalRevenue, tvTotalMenus;
    private CardView cardMyStand, cardManageMenus, cardManageOrders, cardStatistics, cardNotifications;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if logged in
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Check role - redirect if buyer
        if (!sessionManager.isSeller()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_seller_dashboard);

        // Initialize
        dbHelper = new DBHelper(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard Penjual");
        }

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalMenus = findViewById(R.id.tvTotalMenus);
        cardMyStand = findViewById(R.id.cardMyStand);
        cardManageMenus = findViewById(R.id.cardManageMenus);
        cardManageOrders = findViewById(R.id.cardManageOrders);
        cardStatistics = findViewById(R.id.cardStatistics);
        cardNotifications = findViewById(R.id.cardNotifications);

        // Set welcome message
        User user = sessionManager.getUserDetails();
        if (user != null) {
            tvWelcome.setText("Selamat datang, " + user.getName() + "! 👋");
        }

        // Load statistics
        loadStatistics();

        // Setup click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // My Stand
        cardMyStand.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyStandActivity.class);
            startActivity(intent);
        });

        // Manage Menus
        cardManageMenus.setOnClickListener(v -> {
            Intent intent = new Intent(this, SellerManageMenusActivity.class);
            startActivity(intent);
        });

        // Manage Orders
        cardManageOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, SellerManageOrdersActivity.class);
            startActivity(intent);
        });

        // Statistics (placeholder)
        cardStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(this, SellerStatisticsActivity.class);
            startActivity(intent);
        });

        // Notifications (placeholder)
        cardNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        });
    }

    private void loadStatistics() {
        int sellerId = sessionManager.getUserId();

        // Get stand
        Stand stand = dbHelper.getStandBySeller(sellerId);

        if (stand != null) {
            // Total orders
            int totalOrders = dbHelper.getTotalOrdersBySeller(sellerId, "all");
            tvTotalOrders.setText(String.valueOf(totalOrders));

            // Total revenue
            int revenue = dbHelper.getTotalRevenue(sellerId);
            tvTotalRevenue.setText("Rp " + String.format("%,d", revenue));

            // Total menus
            java.util.List<com.example.kantinkampus.Menu> menus = dbHelper.getMenusByStand(stand.getId());
            tvTotalMenus.setText(String.valueOf(menus.size()));
        } else {
            tvTotalOrders.setText("0");
            tvTotalRevenue.setText("Rp 0");
            tvTotalMenus.setText("0");
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.seller_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            showProfile();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProfile() {
        Intent intent = new Intent(this, SellerProfileActivity.class);
        startActivity(intent);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    sessionManager.logoutUser();
                    redirectToLogin();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Keluar Aplikasi")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    super.onBackPressed();
                    finishAffinity();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}