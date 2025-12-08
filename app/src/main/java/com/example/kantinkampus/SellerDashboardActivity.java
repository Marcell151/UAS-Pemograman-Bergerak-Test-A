package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

/**
 * SELLER DASHBOARD ACTIVITY - SAFE VERSION
 * With comprehensive error handling
 */
public class SellerDashboardActivity extends AppCompatActivity {
    private static final String TAG = "SellerDashboard";

    private TextView tvWelcome, tvTotalOrders, tvTotalRevenue, tvTotalMenus;
    private CardView cardMyStand, cardManageMenus, cardManageOrders, cardStatistics, cardNotifications;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate started");

        try {
            // Initialize session manager
            sessionManager = new SessionManager(this);
            Log.d(TAG, "SessionManager initialized");

            // Check if logged in
            if (!sessionManager.isLoggedIn()) {
                Log.d(TAG, "User not logged in, redirecting to login");
                redirectToLogin();
                return;
            }

            // Check role - redirect if buyer
            if (!sessionManager.isSeller()) {
                Log.d(TAG, "User is buyer, redirecting to MainActivity");
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            Log.d(TAG, "User is seller, continuing...");

            // Set content view
            setContentView(R.layout.activity_seller_dashboard);
            Log.d(TAG, "Layout set");

            // Initialize database
            try {
                dbHelper = new DBHelper(this);
                dbHelper.getWritableDatabase(); // Test database
                Log.d(TAG, "Database initialized");
            } catch (Exception e) {
                Log.e(TAG, "Database initialization failed: " + e.getMessage(), e);
                showErrorAndExit("Database error: " + e.getMessage());
                return;
            }

            // Setup toolbar
            try {
                Toolbar toolbar = findViewById(R.id.toolbar);
                if (toolbar != null) {
                    setSupportActionBar(toolbar);
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Dashboard Penjual");
                    }
                    Log.d(TAG, "Toolbar set");
                } else {
                    Log.w(TAG, "Toolbar not found in layout");
                }
            } catch (Exception e) {
                Log.e(TAG, "Toolbar setup error: " + e.getMessage(), e);
            }

            // Initialize views
            initViews();

            // Set welcome message
            try {
                User user = sessionManager.getUserDetails();
                if (user != null && tvWelcome != null) {
                    tvWelcome.setText("Selamat datang, " + user.getName() + "! 👋");
                    Log.d(TAG, "Welcome message set");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting welcome: " + e.getMessage(), e);
            }

            // Load statistics
            loadStatistics();

            // Setup click listeners
            setupClickListeners();

            Log.d(TAG, "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Fatal error in onCreate: " + e.getMessage(), e);
            showErrorAndExit("Error starting dashboard: " + e.getMessage());
        }
    }

    private void initViews() {
        try {
            tvWelcome = findViewById(R.id.tvWelcome);
            tvTotalOrders = findViewById(R.id.tvTotalOrders);
            tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
            tvTotalMenus = findViewById(R.id.tvTotalMenus);
            cardMyStand = findViewById(R.id.cardMyStand);
            cardManageMenus = findViewById(R.id.cardManageMenus);
            cardManageOrders = findViewById(R.id.cardManageOrders);
            cardStatistics = findViewById(R.id.cardStatistics);
            cardNotifications = findViewById(R.id.cardNotifications);

            Log.d(TAG, "Views initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    private void setupClickListeners() {
        try {
            // My Stand
            if (cardMyStand != null) {
                cardMyStand.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, MyStandActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening MyStand: " + e.getMessage(), e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Manage Menus
            if (cardManageMenus != null) {
                cardManageMenus.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, SellerManageMenusActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening ManageMenus: " + e.getMessage(), e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Manage Orders
            if (cardManageOrders != null) {
                cardManageOrders.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, SellerManageOrdersActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening ManageOrders: " + e.getMessage(), e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Statistics
            if (cardStatistics != null) {
                cardStatistics.setOnClickListener(v -> {
                    Toast.makeText(this, "📊 Statistik - Coming Soon", Toast.LENGTH_SHORT).show();
                });
            }

            // Notifications
            if (cardNotifications != null) {
                cardNotifications.setOnClickListener(v -> {
                    Toast.makeText(this, "🔔 Notifikasi - Coming Soon", Toast.LENGTH_SHORT).show();
                });
            }

            Log.d(TAG, "Click listeners set");
        } catch (Exception e) {
            Log.e(TAG, "Error setting click listeners: " + e.getMessage(), e);
        }
    }

    private void loadStatistics() {
        try {
            int sellerId = sessionManager.getUserId();
            Log.d(TAG, "Loading statistics for seller: " + sellerId);

            // Set defaults first
            if (tvTotalOrders != null) tvTotalOrders.setText("0");
            if (tvTotalRevenue != null) tvTotalRevenue.setText("Rp 0");
            if (tvTotalMenus != null) tvTotalMenus.setText("0");

            // Get stand
            Stand stand = null;
            try {
                stand = dbHelper.getStandBySeller(sellerId);
                Log.d(TAG, "Stand loaded: " + (stand != null ? stand.getNama() : "null"));
            } catch (Exception e) {
                Log.e(TAG, "Error loading stand: " + e.getMessage(), e);
            }

            if (stand != null) {
                // Total orders
                try {
                    int totalOrders = dbHelper.getTotalOrdersBySeller(sellerId, "all");
                    if (tvTotalOrders != null) {
                        tvTotalOrders.setText(String.valueOf(totalOrders));
                    }
                    Log.d(TAG, "Total orders: " + totalOrders);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting orders: " + e.getMessage(), e);
                }

                // Total revenue
                try {
                    int revenue = dbHelper.getTotalRevenue(sellerId);
                    if (tvTotalRevenue != null) {
                        tvTotalRevenue.setText("Rp " + String.format("%,d", revenue));
                    }
                    Log.d(TAG, "Total revenue: " + revenue);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting revenue: " + e.getMessage(), e);
                }

                // Total menus
                try {
                    java.util.List<com.example.kantinkampus.Menu> menus =
                            dbHelper.getMenusByStand(stand.getId());
                    if (tvTotalMenus != null) {
                        tvTotalMenus.setText(String.valueOf(menus.size()));
                    }
                    Log.d(TAG, "Total menus: " + menus.size());
                } catch (Exception e) {
                    Log.e(TAG, "Error getting menus: " + e.getMessage(), e);
                }
            } else {
                Log.d(TAG, "No stand found for seller");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadStatistics: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading statistics", Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorAndExit(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (sessionManager != null) {
                        sessionManager.logoutUser();
                    }
                    redirectToLogin();
                })
                .setCancelable(false)
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
        try {
            loadStatistics();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.seller_menu, menu);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating menu: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        try {
            if (id == R.id.action_profile) {
                showProfile();
                return true;
            } else if (id == R.id.action_logout) {
                logout();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in menu selection: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProfile() {
        try {
            User user = sessionManager.getUserDetails();
            if (user == null) return;

            String message = "👤 " + user.getName() + "\n" +
                    "📧 " + user.getEmail() + "\n" +
                    "📱 " + user.getPhone() + "\n" +
                    user.getIdNumberLabel();

            new AlertDialog.Builder(this)
                    .setTitle("Profil Penjual")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing profile: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    try {
                        sessionManager.logoutUser();
                        redirectToLogin();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during logout: " + e.getMessage(), e);
                        finish();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
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