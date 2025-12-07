package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * FAVORITES ACTIVITY
 * Shows user's favorite menus
 */
public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView rvFavorites;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private MenuAdapterBuyer adapter;
    private List<Menu> favoriteMenus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Menu Favorit");
        }

        // Initialize views
        rvFavorites = findViewById(R.id.rvFavorites);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Setup RecyclerView - Grid layout
        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        favoriteMenus = new ArrayList<>();

        // Load favorites
        loadFavorites();
    }

    private void loadFavorites() {
        int userId = sessionManager.getUserId();
        favoriteMenus = dbHelper.getFavoriteMenus(userId);

        if (favoriteMenus.isEmpty()) {
            rvFavorites.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("❤️ Belum ada menu favorit\n\nTap ikon ❤️ pada menu untuk menambahkan ke favorit");
        } else {
            rvFavorites.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            // Setup adapter
            adapter = new MenuAdapterBuyer(this, favoriteMenus, new MenuAdapterBuyer.MenuListener() {
                @Override
                public void onMenuClick(Menu menu) {
                    Intent intent = new Intent(FavoritesActivity.this, MenuDetailActivity.class);
                    intent.putExtra("menu_id", menu.getId());
                    startActivity(intent);
                }

                @Override
                public void onAddToCart(Menu menu) {
                    // Show add to cart dialog (simplified)
                    addToCartQuick(menu);
                }

                @Override
                public void onFavoriteClick(Menu menu) {
                    toggleFavorite(menu);
                }
            });
            rvFavorites.setAdapter(adapter);
        }
    }

    private void addToCartQuick(Menu menu) {
        int userId = sessionManager.getUserId();
        long result = dbHelper.addToCart(userId, menu.getId(), 1, null);

        if (result > 0) {
            android.widget.Toast.makeText(this, "✅ " + menu.getNama() + " ditambahkan ke keranjang",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite(Menu menu) {
        int userId = sessionManager.getUserId();
        int result = dbHelper.removeFromFavorites(userId, menu.getId());

        if (result > 0) {
            android.widget.Toast.makeText(this, "Dihapus dari favorit",
                    android.widget.Toast.LENGTH_SHORT).show();
            loadFavorites(); // Reload list
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites(); // Reload when returning
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