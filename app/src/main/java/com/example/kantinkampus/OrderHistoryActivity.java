package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity implements OrderAdapterCustomer.OnOrderClickListener {
    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;

    private OrderAdapterCustomer adapter;
    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);
        userId = sessionManager.getUserId();

        rvOrders = findViewById(R.id.rvOrders);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders = dbHelper.getOrdersByUser(userId);

        if (orders.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new OrderAdapterCustomer(this, orders, this);
                rvOrders.setAdapter(adapter);
            } else {
                adapter.updateOrders(orders);
            }
        }
    }

    @Override
    public void onOrderClick(Order order) {
        showOrderDetailDialog(order);
    }

    private void showOrderDetailDialog(Order order) {
        List<OrderItem> items = dbHelper.getOrderItems(order.getId());

        StringBuilder itemsText = new StringBuilder();
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        for (OrderItem item : items) {
            itemsText.append("• ")
                    .append(item.getMenuName())
                    .append(" (")
                    .append(item.getQty())
                    .append("x) - ")
                    .append(formatter.format(item.getSubtotal()))
                    .append("\n");
        }

        String message = "Stand: " + order.getStandName() + "\n" +
                "Waktu: " + order.getCreatedAt() + "\n\n" +
                "Items:\n" + itemsText.toString() + "\n" +
                "Payment: " + order.getPaymentMethod() + "\n" +
                "Total: " + formatter.format(order.getTotal()) + "\n\n" +
                "Status: " + order.getStatusText();

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            message += "\n\nCatatan: " + order.getNotes();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(order.getStatusEmoji() + " Detail Pesanan #" + order.getId());
        builder.setMessage(message);

        // Show review option for completed orders
        if (order.isCompleted()) {
            builder.setPositiveButton("⭐ Beri Review", (dialog, which) -> {
                // TODO: Open review activity
                Toast.makeText(this, "Fitur review akan segera hadir", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Tutup", null);
        } else if (order.isPending()) {
            builder.setPositiveButton("❌ Batalkan", (dialog, which) -> {
                confirmCancelOrder(order.getId());
            });
            builder.setNegativeButton("Tutup", null);
        } else {
            builder.setPositiveButton("Tutup", null);
        }

        builder.show();
    }

    private void confirmCancelOrder(int orderId) {
        new AlertDialog.Builder(this)
                .setTitle("Batalkan Pesanan")
                .setMessage("Apakah Anda yakin ingin membatalkan pesanan ini?")
                .setPositiveButton("Ya, Batalkan", (dialog, which) -> {
                    int result = dbHelper.cancelOrder(orderId);
                    if (result > 0) {
                        Toast.makeText(this, "✅ Pesanan dibatalkan", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    } else {
                        Toast.makeText(this, "❌ Gagal membatalkan pesanan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}