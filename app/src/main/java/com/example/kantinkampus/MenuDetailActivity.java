package com.example.kantinkampus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * MENU DETAIL ACTIVITY
 * Shows full menu details with add to cart and favorites
 */
public class MenuDetailActivity extends AppCompatActivity {
    private TextView tvMenuName, tvPrice, tvCategory, tvDescription, tvRating, tvReviewCount;
    private ImageButton btnFavorite, btnCart;
    private Button btnAddToCart;
    private RecyclerView rvReviews;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private Menu menu;
    private int menuId;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_detail);

        // Get menu ID from intent
        menuId = getIntent().getIntExtra("menu_id", -1);
        if (menuId == -1) {
            Toast.makeText(this, "Error: Menu tidak ditemukan", Toast.LENGTH_SHORT).show();
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
            getSupportActionBar().setTitle("Detail Menu");
        }

        // Initialize views
        tvMenuName = findViewById(R.id.tvMenuName);
        tvPrice = findViewById(R.id.tvPrice);
        tvCategory = findViewById(R.id.tvCategory);
        tvDescription = findViewById(R.id.tvDescription);
        tvRating = findViewById(R.id.tvRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnCart = findViewById(R.id.btnCart);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        rvReviews = findViewById(R.id.rvReviews);

        // Setup RecyclerView for reviews
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        // Load menu data
        loadMenuData();

        // Check if favorite
        checkFavoriteStatus();

        // Favorite button
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Cart button (go to cart)
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        // Add to cart button
        btnAddToCart.setOnClickListener(v -> showAddToCartDialog());
    }

    private void loadMenuData() {
        menu = dbHelper.getMenuById(menuId);

        if (menu == null) {
            Toast.makeText(this, "Menu tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvMenuName.setText(menu.getNama());
        tvPrice.setText(formatPrice(menu.getHarga()));
        tvCategory.setText(menu.getKategori());
        tvDescription.setText(menu.getDeskripsi());

        // Rating & reviews
        float rating = menu.getAverageRating();
        int reviewCount = menu.getTotalReviews();

        if (reviewCount > 0) {
            tvRating.setText(String.format("⭐ %.1f", rating));
            tvReviewCount.setText("(" + reviewCount + " ulasan)");
        } else {
            tvRating.setText("⭐ Belum ada rating");
            tvReviewCount.setText("");
        }

        // Load reviews
        loadReviews();

        // Update cart badge
        updateCartBadge();
    }

    private void loadReviews() {
        List<Review> reviews = dbHelper.getMenuReviews(menuId);

        if (!reviews.isEmpty()) {
            // TODO: Create ReviewAdapter and set it here
            // For now, just hide if no reviews
            rvReviews.setVisibility(View.GONE);
        } else {
            rvReviews.setVisibility(View.GONE);
        }
    }

    private void checkFavoriteStatus() {
        int userId = sessionManager.getUserId();
        isFavorite = dbHelper.isFavorite(userId, menuId);
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void toggleFavorite() {
        int userId = sessionManager.getUserId();

        if (isFavorite) {
            // Remove from favorites
            int result = dbHelper.removeFromFavorites(userId, menuId);
            if (result > 0) {
                isFavorite = false;
                updateFavoriteButton();
                Toast.makeText(this, "Dihapus dari favorit", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add to favorites
            long result = dbHelper.addToFavorites(userId, menuId);
            if (result > 0) {
                isFavorite = true;
                updateFavoriteButton();
                Toast.makeText(this, "❤️ Ditambahkan ke favorit", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddToCartDialog() {
        if (!menu.getStatus().equals("available")) {
            Toast.makeText(this, "Menu tidak tersedia saat ini", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);

        TextView tvDialogMenuName = dialogView.findViewById(R.id.tvDialogMenuName);
        TextView tvDialogPrice = dialogView.findViewById(R.id.tvDialogPrice);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        Button btnMinus = dialogView.findViewById(R.id.btnMinus);
        Button btnPlus = dialogView.findViewById(R.id.btnPlus);

        tvDialogMenuName.setText(menu.getNama());
        tvDialogPrice.setText(formatPrice(menu.getHarga()));
        etQuantity.setText("1");

        // Quantity controls
        btnMinus.setOnClickListener(v -> {
            int qty = Integer.parseInt(etQuantity.getText().toString());
            if (qty > 1) {
                etQuantity.setText(String.valueOf(qty - 1));
            }
        });

        btnPlus.setOnClickListener(v -> {
            int qty = Integer.parseInt(etQuantity.getText().toString());
            if (qty < 99) {
                etQuantity.setText(String.valueOf(qty + 1));
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Tambah ke Keranjang")
                .setView(dialogView)
                .setPositiveButton("Tambah", (dialog, which) -> {
                    int qty = Integer.parseInt(etQuantity.getText().toString());
                    String notes = etNotes.getText().toString().trim();
                    addToCart(qty, notes);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void addToCart(int qty, String notes) {
        int userId = sessionManager.getUserId();

        long result = dbHelper.addToCart(userId, menuId, qty, notes);

        if (result > 0) {
            Toast.makeText(this, "✅ Ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show();
            updateCartBadge();
        } else {
            Toast.makeText(this, "Gagal menambahkan ke keranjang", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCartBadge() {
        int userId = sessionManager.getUserId();
        int cartCount = dbHelper.getCartCount(userId);

        if (cartCount > 0) {
            btnCart.setImageResource(android.R.drawable.ic_menu_info_details);
            // TODO: Add badge with count
        } else {
            btnCart.setImageResource(android.R.drawable.ic_menu_info_details);
        }
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price).replace("IDR", "Rp").replace(",00", "");
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