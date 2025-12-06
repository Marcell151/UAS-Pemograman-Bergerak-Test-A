package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {
    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private DBHelper dbHelper;
    private TextView tvTotal, btnCheckout;
    private LinearLayout tvEmptyCart, layoutCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbHelper = new DBHelper(this);

        rvCart = findViewById(R.id.rvCart);
        tvTotal = findViewById(R.id.tvTotal);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        layoutCheckout = findViewById(R.id.layoutCheckout);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        loadCart();

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use CheckoutActivity for better experience
                goToCheckout();

                // Or use direct checkout without separate activity
                // checkoutDirect();
            }
        });
    }

    private void loadCart() {
        List<CartItem> cartItems = dbHelper.getCartItems();

        if (cartItems.isEmpty()) {
            rvCart.setVisibility(View.GONE);
            layoutCheckout.setVisibility(View.GONE);
            tvEmptyCart.setVisibility(View.VISIBLE);
        } else {
            rvCart.setVisibility(View.VISIBLE);
            layoutCheckout.setVisibility(View.VISIBLE);
            tvEmptyCart.setVisibility(View.GONE);

            cartAdapter = new CartAdapter(this, cartItems, this);
            rvCart.setAdapter(cartAdapter);

            updateTotal(cartItems);
        }
    }

    private void updateTotal(List<CartItem> cartItems) {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvTotal.setText(formatter.format(total));
    }

    @Override
    public void onQuantityChanged() {
        loadCart();
    }

    private void goToCheckout() {
        List<CartItem> cartItems = dbHelper.getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Go to checkout activity
        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    // Alternative simple checkout (without CheckoutActivity)
    private void checkoutDirect() {
        List<CartItem> cartItems = dbHelper.getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total
        int total = 0;
        StringBuilder items = new StringBuilder();
        String standName = "";

        for (CartItem item : cartItems) {
            total += item.getSubtotal();
            items.append(item.getMenu().getNama())
                    .append(" (")
                    .append(item.getQty())
                    .append("x)\n");

            // Get stand name from first item
            if (standName.isEmpty()) {
                standName = getStandNameByMenuId(item.getMenu().getStandId());
            }
        }

        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Konfirmasi Pesanan");
        builder.setMessage("Total: " + NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(total) + "\n\nLanjutkan pesanan?");

        final int finalTotal = total;
        final String finalItems = items.toString();
        final String finalStandName = standName;

        builder.setPositiveButton("Ya, Pesan! 🍽️", (dialog, which) -> {
            // Create order in database
            long orderId = dbHelper.createOrder(
                    "Customer", // Default customer name
                    finalStandName,
                    finalItems,
                    finalTotal,
                    "Cash", // Default payment method
                    "" // No notes
            );

            // Add order items
            for (CartItem item : cartItems) {
                dbHelper.addOrderItem(
                        (int) orderId,
                        item.getMenu().getId(),
                        item.getMenu().getNama(),
                        item.getQty(),
                        item.getMenu().getHarga()
                );
            }

            // Save to history (backward compatibility)
            dbHelper.addToHistory(finalItems, finalTotal);

            // Clear cart
            dbHelper.clearCart();

            // Show success message
            Toast.makeText(CartActivity.this, "✅ Pesanan berhasil! Order #" + orderId, Toast.LENGTH_LONG).show();

            // Reload cart
            loadCart();

            // Close activity after delay
            new android.os.Handler().postDelayed(() -> finish(), 1500);
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private String getStandNameByMenuId(int standId) {
        List<Stand> stands = dbHelper.getAllStands();
        for (Stand stand : stands) {
            if (stand.getId() == standId) {
                return stand.getNama();
            }
        }
        return "Stand";
    }
}