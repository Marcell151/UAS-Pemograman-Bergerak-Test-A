package com.example.kantinkampus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.util.List;

/**
 * SELLER MANAGE ORDERS ACTIVITY
 * Verify payments, update order status, process orders
 */
public class SellerManageOrdersActivity extends AppCompatActivity {
    private static final String TAG = "ManageOrders";

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private TabLayout tabLayout;
    private RecyclerView rvOrders;
    private TextView tvEmptyState;

    private OrderAdapterSeller orderAdapter;
    private List<Order> orderList;

    private int sellerId;
    private String currentFilter = "pending_verification"; // Default to pending

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_manage_orders);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kelola Pesanan");
        }

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);
        sellerId = sessionManager.getUserId();

        // Initialize views
        initViews();

        // Setup tabs
        setupTabs();

        // Load orders
        loadOrders(currentFilter);
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        rvOrders = findViewById(R.id.rvOrders);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setHasFixedSize(true);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("⏳ Verifikasi").setTag("pending_verification"));
        tabLayout.addTab(tabLayout.newTab().setText("✅ Verified").setTag("verified"));
        tabLayout.addTab(tabLayout.newTab().setText("👨‍🍳 Cooking").setTag("cooking"));
        tabLayout.addTab(tabLayout.newTab().setText("🎉 Ready").setTag("ready"));
        tabLayout.addTab(tabLayout.newTab().setText("✔️ Selesai").setTag("completed"));
        tabLayout.addTab(tabLayout.newTab().setText("❌ Batal").setTag("cancelled"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = (String) tab.getTag();
                loadOrders(currentFilter);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadOrders(String status) {
        try {
            orderList = dbHelper.getOrdersBySellerAndStatus(sellerId, status);

            if (orderList.isEmpty()) {
                rvOrders.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText(getEmptyMessage(status));
            } else {
                rvOrders.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);

                // Setup adapter with listeners
                orderAdapter = new OrderAdapterSeller(this, orderList,
                        new OrderAdapterSeller.OnOrderClickListener() {
                            @Override
                            public void onOrderClick(Order order) {
                                showOrderDetails(order);
                            }

                            @Override
                            public void onVerifyPayment(Order order) {
                                showVerifyPaymentDialog(order);
                            }

                            @Override
                            public void onUpdateStatus(Order order) {
                                showUpdateStatusDialog(order);
                            }

                            @Override
                            public void onCancelOrder(Order order) {
                                showCancelOrderDialog(order);
                            }
                        });

                rvOrders.setAdapter(orderAdapter);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading orders: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showOrderDetails(Order order) {
        // Get order items
        List<OrderItem> items = dbHelper.getOrderItems(order.getId());
        order.setItems(items);

        // Build details string
        StringBuilder details = new StringBuilder();
        details.append("📦 Order #").append(order.getId()).append("\n\n");
        details.append("👤 Pembeli: ").append(order.getUserName()).append("\n");
        details.append("📅 Tanggal: ").append(order.getCreatedAt()).append("\n");
        details.append("💰 Total: ").append(order.getFormattedTotal()).append("\n\n");

        details.append("🛒 Item Pesanan:\n");
        for (OrderItem item : items) {
            details.append("• ").append(item.getMenuName())
                    .append(" x").append(item.getQty())
                    .append(" = ").append(item.getFormattedSubtotal()).append("\n");
        }

        details.append("\n💳 Metode: ").append(order.getPaymentMethod()).append("\n");
        details.append("📊 Status: ").append(order.getStatusDisplay()).append("\n");

        if (order.getPaymentStatus() != null) {
            details.append("💵 Pembayaran: ").append(order.getPaymentStatus()).append("\n");
        }

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            details.append("\n📝 Catatan: ").append(order.getNotes());
        }

        new AlertDialog.Builder(this)
                .setTitle("Detail Pesanan")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showVerifyPaymentDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("💳 Verifikasi Pembayaran");

        String message = "Order #" + order.getId() + "\n" +
                "Pembeli: " + order.getUserName() + "\n" +
                "Total: " + order.getFormattedTotal() + "\n" +
                "Metode: " + order.getPaymentMethod() + "\n\n";

        if (order.getPaymentProof() != null) {
            message += "✅ Bukti pembayaran telah diupload\n\n";
        } else {
            message += "⚠️ Belum ada bukti pembayaran\n\n";
        }

        message += "Terima pembayaran ini?";

        builder.setMessage(message);

        builder.setPositiveButton("✅ Terima", (dialog, which) -> {
            verifyPayment(order, true, null);
        });

        builder.setNegativeButton("❌ Tolak", (dialog, which) -> {
            showRejectReasonDialog(order);
        });

        builder.setNeutralButton("Batal", null);

        builder.show();
    }

    private void showRejectReasonDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❌ Alasan Penolakan");

        EditText etReason = new EditText(this);
        etReason.setHint("Masukkan alasan penolakan...");
        etReason.setPadding(50, 30, 50, 30);
        builder.setView(etReason);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String reason = etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(this, "❌ Alasan tidak boleh kosong!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            verifyPayment(order, false, reason);
        });

        builder.setNegativeButton("Batal", null);

        builder.show();
    }

    private void verifyPayment(Order order, boolean accepted, String notes) {
        int result = dbHelper.verifyPayment(order.getId(), accepted, notes);

        if (result > 0) {
            String message = accepted ?
                    "✅ Pembayaran diterima! Order sedang diproses." :
                    "❌ Pembayaran ditolak.";

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            loadOrders(currentFilter); // Refresh
        } else {
            Toast.makeText(this, "❌ Gagal memverifikasi pembayaran!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateStatusDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📊 Update Status Pesanan");

        String[] statuses;
        String currentStatus = order.getStatus();

        // Determine next possible statuses
        if ("verified".equals(currentStatus)) {
            statuses = new String[]{"👨‍🍳 Mulai Masak", "❌ Batalkan"};
        } else if ("cooking".equals(currentStatus)) {
            statuses = new String[]{"🎉 Siap Diambil", "❌ Batalkan"};
        } else if ("ready".equals(currentStatus)) {
            statuses = new String[]{"✅ Selesai (Sudah Diambil)"};
        } else {
            Toast.makeText(this, "⚠️ Status tidak dapat diubah",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        builder.setItems(statuses, (dialog, which) -> {
            String newStatus;

            if (statuses[which].contains("Mulai Masak")) {
                newStatus = "cooking";
            } else if (statuses[which].contains("Siap Diambil")) {
                newStatus = "ready";
            } else if (statuses[which].contains("Selesai")) {
                newStatus = "completed";
            } else if (statuses[which].contains("Batalkan")) {
                showCancelOrderDialog(order);
                return;
            } else {
                return;
            }

            updateOrderStatus(order, newStatus);
        });

        builder.setNegativeButton("Batal", null);

        builder.show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        int result = dbHelper.updateOrderStatus(order.getId(), newStatus);

        if (result > 0) {
            String message;
            switch (newStatus) {
                case "cooking":
                    message = "👨‍🍳 Pesanan sedang dimasak";
                    break;
                case "ready":
                    message = "🎉 Pesanan siap diambil!";
                    break;
                case "completed":
                    message = "✅ Pesanan selesai";
                    break;
                default:
                    message = "✅ Status berhasil diupdate";
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            loadOrders(currentFilter); // Refresh
        } else {
            Toast.makeText(this, "❌ Gagal update status!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showCancelOrderDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("❌ Batalkan Pesanan");
        builder.setMessage("Yakin ingin membatalkan pesanan ini?\n\nBerikan alasan pembatalan:");

        EditText etReason = new EditText(this);
        etReason.setHint("Contoh: Bahan habis, Stand tutup, dll");
        etReason.setPadding(50, 30, 50, 30);
        builder.setView(etReason);

        builder.setPositiveButton("Batalkan", (dialog, which) -> {
            String reason = etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(this, "❌ Alasan tidak boleh kosong!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int result = dbHelper.cancelOrder(order.getId(), reason);

            if (result > 0) {
                Toast.makeText(this, "❌ Pesanan dibatalkan",
                        Toast.LENGTH_SHORT).show();
                loadOrders(currentFilter); // Refresh
            } else {
                Toast.makeText(this, "❌ Gagal membatalkan pesanan!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Tidak", null);

        builder.show();
    }

    private String getEmptyMessage(String status) {
        switch (status) {
            case "pending_verification":
                return "🎉 Tidak ada pesanan yang perlu diverifikasi";
            case "verified":
                return "Tidak ada pesanan yang sudah diverifikasi";
            case "cooking":
                return "Tidak ada pesanan yang sedang dimasak";
            case "ready":
                return "Tidak ada pesanan yang siap diambil";
            case "completed":
                return "Belum ada pesanan yang selesai";
            case "cancelled":
                return "Tidak ada pesanan yang dibatalkan";
            default:
                return "Tidak ada pesanan";
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(currentFilter); // Refresh when returning
    }
}