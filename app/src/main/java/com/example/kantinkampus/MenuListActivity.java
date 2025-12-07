package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * MENU LIST ACTIVITY - FIXED
 * Shows all menus from selected stand
 */
public class MenuListActivity extends AppCompatActivity {
    private RecyclerView rvMenus;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage, tvStandName;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private MenuAdapterBuyer adapter;
    private List<Menu> menus;
    private int standId;
    private String standName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);

        // Get stand ID from intent
        standId = getIntent().getIntExtra("stand_id", -1);
        standName = getIntent().getStringExtra("stand_name");

        if (standId == -1) {
            Toast.makeText(this, "Error: Stand tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(standName != null ? standName : "Menu");
        }

        // Initialize views
        tvStandName = findViewById(R.id.tvStandName);
        rvMenus = findViewById(R.id.rvMenus);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Set stand name
        if (standName != null) {
            tvStandName.setText("🏪 " + standName);
        }

        // Setup RecyclerView - Grid layout (2 columns)
        rvMenus.setLayoutManager(new GridLayoutManager(this, 2));
        menus = new ArrayList<>();

        // Load menus
        loadMenus();
    }

    private void loadMenus() {
        menus = dbHelper.getMenusByStand(standId);

        if (menus.isEmpty()) {
            rvMenus.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("Belum ada menu di stand ini");
        } else {
            rvMenus.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            // Setup adapter with CORRECT interface name
            adapter = new MenuAdapterBuyer(this, menus,
                    new MenuAdapterBuyer.MenuListener() { // ✅ FIXED: Use MenuListener
                        @Override
                        public void onMenuClick(Menu menu) {
                            // Open menu detail
                            Intent intent = new Intent(MenuListActivity.this, MenuDetailActivity.class);
                            intent.putExtra("menu_id", menu.getId());
                            startActivity(intent);
                        }

                        @Override
                        public void onAddToCart(Menu menu) {
                            // Quick add to cart (quantity 1, no notes)
                            addToCartQuick(menu);
                        }

                        @Override
                        public void onFavoriteClick(Menu menu) {
                            // Toggle favorite
                            toggleFavorite(menu);
                        }
                    });
            rvMenus.setAdapter(adapter);
        }
    }

    private void addToCartQuick(Menu menu) {
        if (!menu.getStatus().equals("available")) {
            Toast.makeText(this, "Menu tidak tersedia saat ini", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager.getUserId();
        long result = dbHelper.addToCart(userId, menu.getId(), 1, null);

        if (result > 0) {
            Toast.makeText(this, "✅ " + menu.getNama() + " ditambahkan ke keranjang",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal menambahkan ke keranjang", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite(Menu menu) {
        int userId = sessionManager.getUserId();
        boolean isFavorite = dbHelper.isFavorite(userId, menu.getId());

        if (isFavorite) {
            // Remove from favorites
            int result = dbHelper.removeFromFavorites(userId, menu.getId());
            if (result > 0) {
                Toast.makeText(this, "Dihapus dari favorit", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add to favorites
            long result = dbHelper.addToFavorites(userId, menu.getId());
            if (result > 0) {
                Toast.makeText(this, "❤️ Ditambahkan ke favorit", Toast.LENGTH_SHORT).show();
            }
        }

        // Reload to update UI
        loadMenus();
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
        loadMenus(); // Reload when returning
    }
}