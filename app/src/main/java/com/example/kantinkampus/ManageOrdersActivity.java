package com.example.kantinkampus;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ManageOrdersActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private TextView tvTotalOrders, tvTotalRevenue;
    private OrderAdapterAdmin orderAdapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        dbHelper = new DBHelper(this);

        rvOrders = findViewById(R.id.rvOrders);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        loadOrders();
        updateStatistics();
    }

    private void loadOrders() {
        List<Order> orders = dbHelper.getAllOrders();

        // Create simple click listener
        orderAdapter = new OrderAdapterAdmin(this, orders, new OrderAdapterAdmin.OnOrderListener() {
            @Override
            public void onOrderClick(Order order) {
                showOrderOptions(order);
            }
        });
        rvOrders.setAdapter(orderAdapter);
    }

    private void showOrderOptions(Order order) {
        String[] options;

        if (order.isPending()) {
            options = new String[]{"Proses Pesanan", "Batalkan", "Lihat Detail"};
        } else if (order.isProcessing()) {
            options = new String[]{"Siap Diambil", "Batalkan", "Lihat Detail"};
        } else if (order.isReady()) {
            options = new String[]{"Tandai Selesai", "Lihat Detail"};
        } else {
            options = new String[]{"Lihat Detail"};
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Aksi - Order #" + order.getId());
        builder.setItems(options, (dialog, which) -> {
            if (order.isPending() || order.isProcessing()) {
                switch (which) {
                    case 0: // Proses atau Ready
                        if (order.isPending()) {
                            updateOrderStatus(order.getId(), Order.STATUS_PROCESSING);
                        } else {
                            updateOrderStatus(order.getId(), Order.STATUS_READY);
                        }
                        break;
                    case 1: // Cancel
                        showCancelConfirmation(order);
                        break;
                    case 2: // Detail
                        showOrderDetails(order);
                        break;
                }
            } else if (order.isReady()) {
                switch (which) {
                    case 0: // Complete
                        updateOrderStatus(order.getId(), Order.STATUS_COMPLETED);
                        break;
                    case 1: // Detail
                        showOrderDetails(order);
                        break;
                }
            } else {
                showOrderDetails(order);
            }
        });
        builder.show();
    }

    private void updateStatistics() {
        int totalOrders = dbHelper.getTotalOrdersCount();
        int totalRevenue = dbHelper.getTotalRevenue();

        tvTotalOrders.setText(String.valueOf(totalOrders));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvTotalRevenue.setText(formatter.format(totalRevenue));
    }

    private void updateOrderStatus(int orderId, String newStatus) {
        dbHelper.updateOrderStatus(orderId, newStatus);

        String statusText = "";
        switch (newStatus) {
            case Order.STATUS_PROCESSING:
                statusText = "Pesanan sedang diproses";
                break;
            case Order.STATUS_READY:
                statusText = "Pesanan siap diambil";
                break;
            case Order.STATUS_COMPLETED:
                statusText = "Pesanan selesai";
                break;
            case Order.STATUS_CANCELLED:
                statusText = "Pesanan dibatalkan";
                break;
        }

        Toast.makeText(this, "✅ " + statusText, Toast.LENGTH_SHORT).show();
        loadOrders();
        updateStatistics();
    }

    private void showCancelConfirmation(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Batalkan Pesanan");
        builder.setMessage("Yakin ingin membatalkan pesanan #" + order.getId() + "?");

        builder.setPositiveButton("Ya, Batalkan", (dialog, which) -> {
            updateOrderStatus(order.getId(), Order.STATUS_CANCELLED);
        });

        builder.setNegativeButton("Tidak", null);
        builder.show();
    }

    private void showOrderDetails(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detail Pesanan #" + order.getId());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        String message = "Customer: " + order.getUserName() + "\n" +
                "Stand: " + order.getStandName() + "\n" +
                "Tanggal: " + order.getCreatedAt() + "\n" +
                "Status: " + order.getStatusText() + "\n" +
                "Pembayaran: " + order.getPaymentMethod() + "\n\n" +
                "Pesanan:\n" + order.getItems() + "\n" +
                "Total: " + formatter.format(order.getTotal());

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            message += "\n\nCatatan:\n" + order.getNotes();
        }

        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
        updateStatistics();
    }
}