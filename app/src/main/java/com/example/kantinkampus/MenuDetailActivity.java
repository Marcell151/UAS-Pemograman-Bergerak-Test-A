package com.example.kantinkampus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MenuDetailActivity extends AppCompatActivity {
    private TextView tvMenuName, tvMenuPrice, tvMenuDescription;
    private TextView tvAverageRating, tvTotalReviews, btnAddToCart, btnAddReview, btnFavorite;
    private RatingBar ratingBar;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private int menuId;
    private int userId;
    private boolean isFavorite = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_detail);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        // Get menu ID from intent
        menuId = getIntent().getIntExtra("menu_id", -1);
        if (menuId == -1) {
            Toast.makeText(this, "Error: Menu tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvMenuName = findViewById(R.id.tvMenuName);
        tvMenuPrice = findViewById(R.id.tvMenuPrice);
        tvMenuDescription = findViewById(R.id.tvMenuDescription);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);
        ratingBar = findViewById(R.id.ratingBar);
        rvReviews = findViewById(R.id.rvReviews);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnAddReview = findViewById(R.id.btnAddReview);
        btnFavorite = findViewById(R.id.btnFavorite);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        loadMenuDetail();
        loadReviews();
        checkFavoriteStatus();

        // Setup buttons
        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });

        btnAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddReview();
            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });
    }

    private void loadMenuDetail() {
        Menu menu = dbHelper.getMenuById(menuId);
        if (menu == null) {
            Toast.makeText(this, "Menu tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvMenuName.setText(menu.getNama());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvMenuPrice.setText(formatter.format(menu.getHarga()));

        tvMenuDescription.setText(menu.getDeskripsi() != null ? menu.getDeskripsi() : "Tidak ada deskripsi");

        // Rating
        double avgRating = menu.getAverageRating();
        int totalReviews = menu.getTotalReviews();

        ratingBar.setRating((float) avgRating);
        tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", avgRating));
        tvTotalReviews.setText("(" + totalReviews + " review)");

        // Check if menu available
        if (!menu.isAvailable()) {
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Tidak Tersedia");
        }
    }

    private void loadReviews() {
        List<Review> reviews = dbHelper.getMenuReviews(menuId);
        reviewAdapter = new ReviewAdapter(this, reviews);
        rvReviews.setAdapter(reviewAdapter);

        if (reviews.isEmpty()) {
            // If you have a TextView for empty reviews, show it
            rvReviews.setVisibility(View.GONE);
        } else {
            rvReviews.setVisibility(View.VISIBLE);
        }
    }

    private void checkFavoriteStatus() {
        isFavorite = dbHelper.isFavorite(userId, menuId);
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            btnFavorite.setText("♥ Hapus dari Favorit");
        } else {
            btnFavorite.setText("♡ Tambah ke Favorit");
        }
    }

    private void toggleFavorite() {
        if (isFavorite) {
            // Remove from favorites
            dbHelper.removeFromFavorites(userId, menuId);
            isFavorite = false;
            Toast.makeText(this, "❌ Dihapus dari favorit", Toast.LENGTH_SHORT).show();
        } else {
            // Add to favorites
            dbHelper.addToFavorites(userId, menuId);
            isFavorite = true;
            Toast.makeText(this, "✅ Ditambahkan ke favorit", Toast.LENGTH_SHORT).show();
        }
        updateFavoriteButton();
    }

    private void addToCart() {
        dbHelper.addToCart(userId, menuId, 1, null);
        Toast.makeText(this, "✅ Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
    }

    private void openAddReview() {
        Menu menu = dbHelper.getMenuById(menuId);
        Intent intent = new Intent(MenuDetailActivity.this, AddReviewActivity.class);
        intent.putExtra("menu_id", menuId);
        intent.putExtra("menu_name", menu.getNama());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenuDetail();
        loadReviews();
        checkFavoriteStatus();
    }
}