package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * MENU LIST ACTIVITY - Browse Menus from Selected Stand
 * Shows all available menus with quick add to cart
 */
public class MenuListActivity extends AppCompatActivity {
    private static final String TAG = "MenuListActivity";

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    private RecyclerView rvMenus;
    private TextView tvEmptyState, tvStandName;

    private MenuAdapterBuyer menuAdapter;
    private List<Menu> menuList;

    private int standId;
    private String standName;
    private int buyerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);

        // Get extras
        standId = getIntent().getIntExtra("stand_id", -1);
        standName = getIntent().getStringExtra("stand_name");

        if (standId == -1) {
            Toast.makeText(this, "⚠️ Stand tidak ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(standName);
        }

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);
        buyerId = sessionManager.getUserId();

        // Initialize views
        initViews();

        // Load menus
        loadMenus();
    }

    private void initViews() {
        rvMenus = findViewById(R.id.rvMenus);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvStandName = findViewById(R.id.tvStandName);

        // Set stand name
        tvStandName.setText(standName);

        // Setup RecyclerView
        rvMenus.setLayoutManager(new LinearLayoutManager(this));
        rvMenus.setHasFixedSize(true);
    }

    private void loadMenus() {
        try {
            menuList = dbHelper.getMenusByStand(standId);

            if (menuList.isEmpty()) {
                rvMenus.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvMenus.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);

                // Setup adapter
                menuAdapter = new MenuAdapterBuyer(this, menuList,
                        new MenuAdapterBuyer.OnMenuClickListener() {
                            @Override
                            public void onMenuClick(Menu menu) {
                                // Navigate to menu detail
                                Intent intent = new Intent(MenuListActivity.this, MenuDetailActivity.class);
                                intent.putExtra("menu_id", menu.getId());
                                startActivity(intent);
                            }

                            @Override
                            public void onAddToCart(Menu menu) {
                                showAddToCartDialog(menu);
                            }

                            @Override
                            public void onToggleFavorite(Menu menu) {
                                toggleFavorite(menu);
                            }
                        });

                rvMenus.setAdapter(menuAdapter);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading menus: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddToCartDialog(Menu menu) {
        if (!menu.isAvailable()) {
            Toast.makeText(this, "⚠️ Menu tidak tersedia saat ini",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🛒 Tambah ke Keranjang");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_to_cart, null);
        builder.setView(dialogView);

        TextView tvMenuName = dialogView.findViewById(R.id.tvMenuName);
        TextView tvMenuPrice = dialogView.findViewById(R.id.tvMenuPrice);
        EditText etQty = dialogView.findViewById(R.id.etQty);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        tvMenuName.setText(menu.getNama());
        tvMenuPrice.setText(menu.getFormattedPrice());
        etQty.setText("1");

        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String qtyStr = etQty.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (qtyStr.isEmpty()) {
                Toast.makeText(this, "❌ Jumlah harus diisi!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    Toast.makeText(this, "❌ Jumlah minimal 1!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "❌ Jumlah tidak valid!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Add to cart
            long result = dbHelper.addToCart(buyerId, menu.getId(), qty, notes);

            if (result > 0) {
                Toast.makeText(this, "✅ Ditambahkan ke keranjang!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Gagal menambahkan!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);

        builder.show();
    }

    private void toggleFavorite(Menu menu) {
        boolean isFavorite = dbHelper.isFavorite(buyerId, menu.getId());

        if (isFavorite) {
            // Remove from favorites
            int result = dbHelper.removeFromFavorites(buyerId, menu.getId());
            if (result > 0) {
                Toast.makeText(this, "💔 Dihapus dari favorit",
                        Toast.LENGTH_SHORT).show();
                loadMenus(); // Refresh
            }
        } else {
            // Add to favorites
            long result = dbHelper.addToFavorites(buyerId, menu.getId());
            if (result > 0) {
                Toast.makeText(this, "❤️ Ditambahkan ke favorit!",
                        Toast.LENGTH_SHORT).show();
                loadMenus(); // Refresh
            }
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
        loadMenus(); // Refresh when returning
    }
}