package com.example.kantinkampus;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * BUYER ORDERS ACTIVITY
 * Shows buyer's order history with status filtering
 */
public class BuyerOrdersActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private OrderAdapterBuyer adapter;
    private List<Order> orders;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_orders);

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pesanan Saya");
        }

        // Initialize views
        tabLayout = findViewById(R.id.tabLayout);
        rvOrders = findViewById(R.id.rvOrders);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Setup RecyclerView
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orders = new ArrayList<>();

        // Setup tabs
        setupTabs();

        // Load initial orders
        loadOrders(currentFilter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Semua"));
        tabLayout.addTab(tabLayout.newTab().setText("Menunggu"));
        tabLayout.addTab(tabLayout.newTab().setText("Diproses"));
        tabLayout.addTab(tabLayout.newTab().setText("Siap"));
        tabLayout.addTab(tabLayout.newTab().setText("Selesai"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "all";
                        break;
                    case 1:
                        currentFilter = "pending";
                        break;
                    case 2:
                        currentFilter = "cooking";
                        break;
                    case 3:
                        currentFilter = "ready";
                        break;
                    case 4:
                        currentFilter = "completed";
                        break;
                }
                loadOrders(currentFilter);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadOrders(String filter) {
        int userId = sessionManager.getUserId();
        List<Order> allOrders = dbHelper.getOrdersByBuyer(userId);

        orders.clear();

        if (filter.equals("all")) {
            orders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                String status = order.getStatus();

                switch (filter) {
                    case "pending":
                        if (status.equals("pending_payment") ||
                                status.equals("pending_verification") ||
                                status.equals("verified")) {
                            orders.add(order);
                        }
                        break;
                    case "cooking":
                        if (status.equals("cooking")) {
                            orders.add(order);
                        }
                        break;
                    case "ready":
                        if (status.equals("ready")) {
                            orders.add(order);
                        }
                        break;
                    case "completed":
                        if (status.equals("completed")) {
                            orders.add(order);
                        }
                        break;
                }
            }
        }

        if (orders.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);

            String emptyMsg = "Belum ada pesanan";
            switch (filter) {
                case "pending":
                    emptyMsg = "Tidak ada pesanan yang menunggu";
                    break;
                case "cooking":
                    emptyMsg = "Tidak ada pesanan yang sedang diproses";
                    break;
                case "ready":
                    emptyMsg = "Tidak ada pesanan yang siap";
                    break;
                case "completed":
                    emptyMsg = "Belum ada pesanan selesai";
                    break;
            }
            tvEmptyMessage.setText(emptyMsg);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            // Setup adapter
            adapter = new OrderAdapterBuyer(this, orders, order -> {
                // Show order details
                showOrderDetails(order);
            });
            rvOrders.setAdapter(adapter);
        }
    }

    private void showOrderDetails(Order order) {
        // Get order items
        List<OrderItem> items = dbHelper.getOrderItems(order.getId());

        StringBuilder details = new StringBuilder();
        details.append("📦 Pesanan #").append(order.getId()).append("\n");
        details.append("🏪 ").append(order.getStandName()).append("\n\n");
        details.append("📋 Item Pesanan:\n");

        for (OrderItem item : items) {
            details.append("  • ").append(item.getMenuName())
                    .append(" x").append(item.getQty())
                    .append(" = Rp ").append(String.format("%,d", item.getSubtotal()))
                    .append("\n");
        }

        details.append("\n💰 Total: Rp ").append(String.format("%,d", order.getTotal()));
        details.append("\n💳 Pembayaran: ").append(order.getPaymentMethod());
        details.append("\n📊 Status: ").append(getStatusText(order.getStatus()));
        details.append("\n📅 ").append(order.getCreatedAt());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Detail Pesanan")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending_payment":
                return "⏳ Menunggu Pembayaran";
            case "pending_verification":
                return "🔍 Menunggu Verifikasi";
            case "verified":
                return "✅ Terverifikasi";
            case "cooking":
                return "👨‍🍳 Sedang Dimasak";
            case "ready":
                return "✅ Siap Diambil";
            case "completed":
                return "🎉 Selesai";
            case "cancelled":
                return "❌ Dibatalkan";
            default:
                return status;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(currentFilter);
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