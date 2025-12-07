package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

/**
 * MAIN ACTIVITY - Buyer Home
 * Dashboard for buyer with navigation to all features
 */
public class MainActivity extends AppCompatActivity {
    private TextView tvWelcome, tvCartCount;
    private CardView cardBrowseStands, cardCart, cardOrders, cardFavorites, cardProfile;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if logged in
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Check role - redirect if seller
        if (sessionManager.isSeller()) {
            Intent intent = new Intent(this, SellerDashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialize
        dbHelper = new DBHelper(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Kantin KampusKu");
        }

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvCartCount = findViewById(R.id.tvCartCount);
        cardBrowseStands = findViewById(R.id.cardBrowseStands);
        cardCart = findViewById(R.id.cardCart);
        cardOrders = findViewById(R.id.cardOrders);
        cardFavorites = findViewById(R.id.cardFavorites);
        cardProfile = findViewById(R.id.cardProfile);

        // Set welcome message
        User user = sessionManager.getUserDetails();
        if (user != null) {
            String greeting = "Halo, " + user.getName() + "! 👋\n" + user.getRoleDisplay();
            tvWelcome.setText(greeting);
        }

        // Update cart count
        updateCartCount();

        // Setup click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Browse Stands
        cardBrowseStands.setOnClickListener(v -> {
            Intent intent = new Intent(this, StandListActivity.class);
            startActivity(intent);
        });

        // Cart
        cardCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        // Orders
        cardOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, BuyerOrdersActivity.class);
            startActivity(intent);
        });

        // Favorites
        cardFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
        });

        // Profile (logout for now)
        cardProfile.setOnClickListener(v -> showProfileOptions());
    }

    private void updateCartCount() {
        int userId = sessionManager.getUserId();
        int cartCount = dbHelper.getCartCount(userId);
        tvCartCount.setText(cartCount + " item");
    }

    private void showProfileOptions() {
        User user = sessionManager.getUserDetails();
        if (user == null) return;

        String message = "👤 " + user.getName() + "\n" +
                "📧 " + user.getEmail() + "\n" +
                "📱 " + user.getPhone() + "\n" +
                user.getIdNumberLabel();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Profil Saya")
                .setMessage(message)
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Tutup", null)
                .show();
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    sessionManager.logoutUser();
                    redirectToLogin();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount(); // Update when returning to activity
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.buyer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cart) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Confirm exit
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Keluar Aplikasi")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    super.onBackPressed();
                    finishAffinity();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}