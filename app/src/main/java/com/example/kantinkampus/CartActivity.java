package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CART ACTIVITY - Multi-Stand Support
 * Groups cart items by stand for separate checkout
 */
public class CartActivity extends AppCompatActivity {
    private RecyclerView rvCart;
    private LinearLayout layoutEmpty;
    private TextView tvTotal, btnCheckout, tvEmptyMessage;
    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private CartAdapter adapter;
    private List<CartItemGroup> cartGroups;
    private int totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Keranjang Belanja");
        }

        // Initialize views
        rvCart = findViewById(R.id.rvCart);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Setup RecyclerView
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartGroups = new ArrayList<>();

        // Load cart
        loadCart();

        // Checkout button
        btnCheckout.setOnClickListener(v -> {
            if (cartGroups.isEmpty()) {
                Toast.makeText(this, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show();
            } else {
                proceedToCheckout();
            }
        });
    }

    private void loadCart() {
        int userId = sessionManager.getUserId();

        // Get cart items grouped by stand
        Map<Integer, List<CartItem>> groupedItems = dbHelper.getCartItemsGroupedByStand(userId);

        cartGroups.clear();
        totalAmount = 0;

        if (groupedItems.isEmpty()) {
            // Show empty state
            rvCart.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvTotal.setText("Rp 0");
            btnCheckout.setEnabled(false);
            tvEmptyMessage.setText("🛒 Keranjang Anda masih kosong\n\nYuk, mulai belanja di stand favorit Anda!");
        } else {
            // Show cart with grouped items
            rvCart.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            btnCheckout.setEnabled(true);

            for (Map.Entry<Integer, List<CartItem>> entry : groupedItems.entrySet()) {
                int standId = entry.getKey();
                List<CartItem> items = entry.getValue();

                // Get stand info
                Stand stand = dbHelper.getStandById(standId);
                if (stand != null) {
                    // Calculate subtotal for this stand
                    int subtotal = 0;
                    for (CartItem item : items) {
                        subtotal += item.getSubtotal();
                    }

                    CartItemGroup group = new CartItemGroup();
                    group.setStandId(standId);
                    group.setStandName(stand.getNama());
                    group.setItems(items);
                    group.setSubtotal(subtotal);

                    cartGroups.add(group);
                    totalAmount += subtotal;
                }
            }

            // Setup adapter
            adapter = new CartAdapter(this, cartGroups, new CartAdapter.CartListener() {
                @Override
                public void onQuantityChanged(CartItem item, int newQty) {
                    updateQuantity(item, newQty);
                }

                @Override
                public void onItemRemoved(CartItem item) {
                    removeItem(item);
                }
            });
            rvCart.setAdapter(adapter);

            // Update total
            updateTotal();
        }
    }

    private void updateQuantity(CartItem item, int newQty) {
        if (newQty <= 0) {
            removeItem(item);
            return;
        }

        int result = dbHelper.updateCartQty(item.getId(), newQty);
        if (result > 0) {
            Toast.makeText(this, "Jumlah diperbarui", Toast.LENGTH_SHORT).show();
            loadCart(); // Reload cart
        } else {
            Toast.makeText(this, "Gagal memperbarui jumlah", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeItem(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Item")
                .setMessage("Hapus " + item.getMenu().getNama() + " dari keranjang?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    int result = dbHelper.updateCartQty(item.getId(), 0);
                    if (result > 0) {
                        Toast.makeText(this, "Item dihapus", Toast.LENGTH_SHORT).show();
                        loadCart();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void updateTotal() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedTotal = formatter.format(totalAmount);
        tvTotal.setText(formattedTotal.replace("IDR", "Rp").replace(",00", ""));
    }

    private void proceedToCheckout() {
        if (cartGroups.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show checkout summary
        StringBuilder message = new StringBuilder();
        message.append("📦 Ringkasan Pesanan:\n\n");

        for (CartItemGroup group : cartGroups) {
            message.append("🏪 ").append(group.getStandName()).append("\n");
            message.append("   ").append(group.getItems().size()).append(" item - ");
            message.append(formatPrice(group.getSubtotal())).append("\n\n");
        }

        message.append("💰 Total: ").append(formatPrice(totalAmount)).append("\n\n");
        message.append("Lanjutkan ke pembayaran?");

        new AlertDialog.Builder(this)
                .setTitle("Checkout")
                .setMessage(message.toString())
                .setPositiveButton("Ya, Lanjutkan", (dialog, which) -> {
                    Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                    intent.putExtra("total", totalAmount);
                    startActivity(intent);
                })
                .setNegativeButton("Cek Lagi", null)
                .show();
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price).replace("IDR", "Rp").replace(",00", "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart(); // Reload when returning to activity
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

