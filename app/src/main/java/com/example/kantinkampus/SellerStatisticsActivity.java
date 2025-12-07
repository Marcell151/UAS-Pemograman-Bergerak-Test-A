package com.example.kantinkampus;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

/**
 * SELLER STATISTICS ACTIVITY
 * Shows detailed statistics and analytics for seller
 */
public class SellerStatisticsActivity extends AppCompatActivity {
    private TextView tvTotalOrders, tvTodayOrders, tvTotalRevenue, tvTodayRevenue;
    private TextView tvPendingOrders, tvCompletedOrders, tvCancelledOrders;
    private TextView tvTotalMenus, tvAvailableMenus, tvUnavailableMenus;
    private TextView tvAverageOrderValue, tvTopSellingMenu;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_statistics);

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Statistik Penjualan");
        }

        // Initialize views
        initViews();

        // Load statistics
        loadStatistics();
    }

    private void initViews() {
        // Orders
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTodayOrders = findViewById(R.id.tvTodayOrders);
        tvPendingOrders = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        tvCancelledOrders = findViewById(R.id.tvCancelledOrders);

        // Revenue
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue);
        tvAverageOrderValue = findViewById(R.id.tvAverageOrderValue);

        // Menus
        tvTotalMenus = findViewById(R.id.tvTotalMenus);
        tvAvailableMenus = findViewById(R.id.tvAvailableMenus);
        tvUnavailableMenus = findViewById(R.id.tvUnavailableMenus);

        // Other
        tvTopSellingMenu = findViewById(R.id.tvTopSellingMenu);
    }

    private void loadStatistics() {
        int sellerId = sessionManager.getUserId();

        // Get stand
        Stand stand = dbHelper.getStandBySeller(sellerId);

        if (stand != null) {
            // Orders statistics
            int totalOrders = dbHelper.getTotalOrdersBySeller(sellerId, "all");
            int todayOrders = dbHelper.getTotalOrdersBySeller(sellerId, "completed"); // Simplified
            int pendingOrders = dbHelper.getTotalOrdersBySeller(sellerId, "pending_verification");
            int completedOrders = dbHelper.getTotalOrdersBySeller(sellerId, "completed");
            int cancelledOrders = dbHelper.getTotalOrdersBySeller(sellerId, "cancelled");

            tvTotalOrders.setText(String.valueOf(totalOrders));
            tvTodayOrders.setText(String.valueOf(todayOrders));
            tvPendingOrders.setText(String.valueOf(pendingOrders));
            tvCompletedOrders.setText(String.valueOf(completedOrders));
            tvCancelledOrders.setText(String.valueOf(cancelledOrders));

            // Revenue statistics
            int totalRevenue = dbHelper.getTotalRevenue(sellerId);
            int todayRevenue = dbHelper.getTodayRevenue(sellerId);
            int avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

            tvTotalRevenue.setText(formatPrice(totalRevenue));
            tvTodayRevenue.setText(formatPrice(todayRevenue));
            tvAverageOrderValue.setText(formatPrice(avgOrderValue));

            // Menu statistics
            java.util.List<com.example.kantinkampus.Menu> allMenus = dbHelper.getMenusByStand(stand.getId());
            int totalMenus = allMenus.size();
            int availableMenus = 0;
            int unavailableMenus = 0;

            for (com.example.kantinkampus.Menu menu : allMenus) {
                if (menu.getStatus().equals("available")) {
                    availableMenus++;
                } else {
                    unavailableMenus++;
                }
            }

            tvTotalMenus.setText(String.valueOf(totalMenus));
            tvAvailableMenus.setText(String.valueOf(availableMenus));
            tvUnavailableMenus.setText(String.valueOf(unavailableMenus));

            // Top selling menu (simplified - just show first menu for now)
            if (!allMenus.isEmpty()) {
                tvTopSellingMenu.setText(allMenus.get(0).getNama());
            } else {
                tvTopSellingMenu.setText("Belum ada menu");
            }
        } else {
            // No stand yet
            setAllStatsToZero();
        }
    }

    private void setAllStatsToZero() {
        tvTotalOrders.setText("0");
        tvTodayOrders.setText("0");
        tvPendingOrders.setText("0");
        tvCompletedOrders.setText("0");
        tvCancelledOrders.setText("0");
        tvTotalRevenue.setText("Rp 0");
        tvTodayRevenue.setText("Rp 0");
        tvAverageOrderValue.setText("Rp 0");
        tvTotalMenus.setText("0");
        tvAvailableMenus.setText("0");
        tvUnavailableMenus.setText("0");
        tvTopSellingMenu.setText("Belum ada data");
    }

    private String formatPrice(int price) {
        return "Rp " + String.format("%,d", price);
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