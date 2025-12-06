package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements MenuAdapter.OnMenuClickListener {
    private RecyclerView rvFavorites;
    private LinearLayout layoutEmpty;

    private MenuAdapter adapter;
    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);
        userId = sessionManager.getUserId();

        rvFavorites = findViewById(R.id.rvFavorites);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));

        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        List<Menu> favoriteMenus = dbHelper.getFavoriteMenus(userId);

        if (favoriteMenus.isEmpty()) {
            rvFavorites.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvFavorites.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new MenuAdapter(this, favoriteMenus, this);
                rvFavorites.setAdapter(adapter);
            } else {
                // Update adapter with new data
                adapter = new MenuAdapter(this, favoriteMenus, this);
                rvFavorites.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onAddToCart(Menu menu) {
        if (menu.isAvailable()) {
            dbHelper.addToCart(userId, menu.getId(), 1, null);
            Toast.makeText(this, "✅ " + menu.getNama() + " ditambahkan ke keranjang!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Menu tidak tersedia", Toast.LENGTH_SHORT).show();
        }
    }
}