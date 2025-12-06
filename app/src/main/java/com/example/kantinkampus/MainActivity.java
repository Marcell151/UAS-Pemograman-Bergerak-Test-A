package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * MAIN ACTIVITY - Buyer Home Screen
 * Main hub for buyers to access all features
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private TextView tvWelcome, tvUserType, tvCartCount, tvOrderCount;
    private CardView cardBrowseStands, cardMyCart, cardMyOrders,
            cardFavorites, cardProfile;

    private int buyerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        // Check if buyer is logged in
        if (!sessionManager.isBuyer()) {
            // Not a buyer, redirect to login
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        buyerId = sessionManager.getUserId();

        // Initialize views
        initViews();

        // Load user data
        loadUserData();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserType = findViewById(R.id.tvUserType);
        tvCartCount = findViewById(R.id.tvCartCount);
        tvOrderCount = findViewById(R.id.tvOrderCount);

        cardBrowseStands = findViewById(R.id.cardBrowseStands);
        cardMyCart = findViewById(R.id.cardMyCart);
        cardMyOrders = findViewById(R.id.cardMyOrders);
        cardFavorites = findViewById(R.id.cardFavorites);
        cardProfile = findViewById(R.id.cardProfile);
    }

    private void loadUserData() {
        // Set welcome message
        tvWelcome.setText(sessionManager.getGreetingMessage());

        // Set user type
        User user = sessionManager.getUserDetails();
        if (user != null) {
            String typeText = user.getRoleDisplay() + " • " + user.getIdNumberLabel();
            tvUserType.setText(typeText);
        }

        // Load cart count
        int cartCount = dbHelper.getCartCount(buyerId);
        tvCartCount.setText(String.valueOf(cartCount));

        // Load active orders count
        int orderCount = dbHelper.getOrdersByBuyer(buyerId).size();
        tvOrderCount.setText(String.valueOf(orderCount));
    }

    private void setupListeners() {
        cardBrowseStands.setOnClickListener(v -> {
            Intent intent = new Intent(this, StandListActivity.class);
            startActivity(intent);
        });

        cardMyCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        cardMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, BuyerOrdersActivity.class);
            startActivity(intent);
        });

        cardFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
        });

        cardProfile.setOnClickListener(v -> {
            showProfileDialog();
        });
    }

    private void showProfileDialog() {
        User user = sessionManager.getUserDetails();
        if (user == null) return;

        StringBuilder profile = new StringBuilder();
        profile.append("👤 Nama: ").append(user.getName()).append("\n\n");
        profile.append("📧 Email: ").append(user.getEmail()).append("\n\n");
        profile.append("📞 HP: ").append(user.getPhone()).append("\n\n");
        profile.append("🎓 Tipe: ").append(user.getRoleDisplay()).append("\n\n");
        profile.append(user.getIdNumberLabel());

        new AlertDialog.Builder(this)
                .setTitle("Profile Saya")
                .setMessage(profile.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.buyer_menu, menu);

        // Update cart badge
        MenuItem cartItem = menu.findItem(R.id.action_cart);
        if (cartItem != null) {
            int count = dbHelper.getCartCount(buyerId);
            if (count > 0) {
                cartItem.setTitle("🛒 (" + count + ")");
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cart) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_notifications) {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
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
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning
        loadUserData();
        invalidateOptionsMenu(); // Update cart badge
    }

    @Override
    public void onBackPressed() {
        // Show exit confirmation
        new AlertDialog.Builder(this)
                .setTitle("Keluar Aplikasi")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    moveTaskToBack(true);
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}