package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.List;

/**
 * MY STAND ACTIVITY - View & Edit Stand Information
 * Shows stand details, statistics, and allows editing
 */
public class MyStandActivity extends AppCompatActivity {
    private static final String TAG = "MyStandActivity";

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private ImageView ivStandImage;
    private TextView tvStandName, tvStandDescription, tvTotalMenus,
            tvTotalOrders, tvTotalRevenue, tvSellerName, tvSellerPhone;
    private CardView cardEditStand, cardViewMenus;

    private int sellerId;
    private Stand myStand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stand);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Stand Saya");
        }

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);
        sellerId = sessionManager.getUserId();

        // Initialize views
        initViews();

        // Load stand data
        loadStandData();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        ivStandImage = findViewById(R.id.ivStandImage);
        tvStandName = findViewById(R.id.tvStandName);
        tvStandDescription = findViewById(R.id.tvStandDescription);
        tvTotalMenus = findViewById(R.id.tvTotalMenus);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvSellerName = findViewById(R.id.tvSellerName);
        tvSellerPhone = findViewById(R.id.tvSellerPhone);

        cardEditStand = findViewById(R.id.cardEditStand);
        cardViewMenus = findViewById(R.id.cardViewMenus);
    }

    private void loadStandData() {
        try {
            myStand = dbHelper.getStandBySeller(sellerId);

            if (myStand == null) {
                Toast.makeText(this, "⚠️ Stand tidak ditemukan!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Display stand info
            tvStandName.setText(myStand.getNama());
            tvStandDescription.setText(myStand.getDeskripsi() != null ?
                    myStand.getDeskripsi() : "Tidak ada deskripsi");

            // Load stand image (placeholder for now)
            // TODO: Load actual image if available
            ivStandImage.setImageResource(R.drawable.ic_book_placeholder);

            // Load seller info
            User seller = dbHelper.getUserById(sellerId);
            if (seller != null) {
                tvSellerName.setText(seller.getName());
                tvSellerPhone.setText(seller.getPhone());
            }

            // Load statistics
            loadStatistics();

        } catch (Exception e) {
            Log.e(TAG, "Error loading stand data: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStatistics() {
        try {
            // Total menus
            List<com.example.kantinkampus.Menu> menus = dbHelper.getMenusByStand(myStand.getId());
            tvTotalMenus.setText(String.valueOf(menus.size()) + " menu");

            // Total orders
            List<Order> orders = dbHelper.getOrdersBySeller(sellerId);
            tvTotalOrders.setText(String.valueOf(orders.size()) + " pesanan");

            // Total revenue
            int revenue = dbHelper.getTotalRevenue(sellerId);
            tvTotalRevenue.setText("Rp " + formatPrice(revenue));

        } catch (Exception e) {
            Log.e(TAG, "Error loading statistics: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        cardEditStand.setOnClickListener(v -> showEditStandDialog());
        cardViewMenus.setOnClickListener(v -> openManageMenus());
    }

    private void showEditStandDialog() {
        // Create dialog for editing stand info
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ Edit Stand");

        // Create custom layout for dialog
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_edit_stand, null);
        builder.setView(dialogView);

        // Get views from dialog
        android.widget.EditText etName = dialogView.findViewById(R.id.etStandName);
        android.widget.EditText etDescription = dialogView.findViewById(R.id.etStandDescription);

        // Set current values
        etName.setText(myStand.getNama());
        etDescription.setText(myStand.getDeskripsi());

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "❌ Nama stand tidak boleh kosong!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Update stand
            int result = dbHelper.updateStand(myStand.getId(), name, description, null);

            if (result > 0) {
                Toast.makeText(this, "✅ Stand berhasil diupdate!",
                        Toast.LENGTH_SHORT).show();
                loadStandData(); // Refresh
            } else {
                Toast.makeText(this, "❌ Gagal update stand!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void openManageMenus() {
        Intent intent = new Intent(this, SellerManageMenusActivity.class);
        intent.putExtra("stand_id", myStand.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.seller_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_profile) {
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
                    Intent intent = new Intent(MyStandActivity.this, LoginActivity.class);
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
        // Refresh data when returning
        loadStandData();
    }
}