package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * CREATE STAND ACTIVITY - First-time Stand Setup
 * Seller can only create ONE stand per account
 */
public class CreateStandActivity extends AppCompatActivity {
    private static final String TAG = "CreateStandActivity";

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private EditText etStandName, etStandDescription;
    private TextView btnCreateStand, tvUserInfo;

    private int sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stand);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Buat Stand Baru");
        }

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);
        sellerId = sessionManager.getUserId();

        // Check if seller is logged in
        if (!sessionManager.isSeller()) {
            Toast.makeText(this, "Akses ditolak! Anda bukan seller.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if already has stand
        checkExistingStand();

        // Initialize views
        initViews();

        // Setup listeners
        setupListeners();
    }

    private void checkExistingStand() {
        Stand existingStand = dbHelper.getStandBySeller(sellerId);

        if (existingStand != null) {
            Toast.makeText(this,
                    "⚠️ Anda sudah memiliki stand: " + existingStand.getNama(),
                    Toast.LENGTH_LONG).show();

            // Redirect to MyStandActivity
            Intent intent = new Intent(this, MyStandActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initViews() {
        etStandName = findViewById(R.id.etStandName);
        etStandDescription = findViewById(R.id.etStandDescription);
        btnCreateStand = findViewById(R.id.btnCreateStand);
        tvUserInfo = findViewById(R.id.tvUserInfo);

        // Show user info
        User seller = dbHelper.getUserById(sellerId);
        if (seller != null) {
            tvUserInfo.setText("Pemilik: " + seller.getName() + "\n" +
                    seller.getIdNumberLabel());
        }
    }

    private void setupListeners() {
        btnCreateStand.setOnClickListener(v -> attemptCreateStand());
    }

    private void attemptCreateStand() {
        // Reset errors
        etStandName.setError(null);
        etStandDescription.setError(null);

        // Get values
        String name = etStandName.getText().toString().trim();
        String description = etStandDescription.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Validate stand name
        if (TextUtils.isEmpty(name)) {
            etStandName.setError("Nama stand tidak boleh kosong");
            focusView = etStandName;
            cancel = true;
        } else if (name.length() < 3) {
            etStandName.setError("Nama stand minimal 3 karakter");
            focusView = etStandName;
            cancel = true;
        } else if (name.length() > 50) {
            etStandName.setError("Nama stand maksimal 50 karakter");
            focusView = etStandName;
            cancel = true;
        }

        // Validate description (optional but recommended)
        if (!TextUtils.isEmpty(description) && description.length() > 200) {
            etStandDescription.setError("Deskripsi maksimal 200 karakter");
            focusView = etStandDescription;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            performCreateStand(name, description);
        }
    }

    private void performCreateStand(String name, String description) {
        try {
            // Show loading
            btnCreateStand.setEnabled(false);
            btnCreateStand.setText("Membuat Stand...");

            // Create stand
            long standId = dbHelper.createStand(sellerId, name, description, null);

            if (standId > 0) {
                // Success
                Toast.makeText(this,
                        "✅ Stand berhasil dibuat!\n" +
                                "Sekarang Anda dapat menambahkan menu.",
                        Toast.LENGTH_LONG).show();

                // Redirect to Seller Dashboard
                Intent intent = new Intent(CreateStandActivity.this, SellerDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else if (standId == -1) {
                // Already has stand
                Toast.makeText(this,
                        "❌ Anda sudah memiliki stand!\n" +
                                "Setiap seller hanya boleh memiliki 1 stand.",
                        Toast.LENGTH_LONG).show();

                // Redirect to MyStandActivity
                Intent intent = new Intent(this, MyStandActivity.class);
                startActivity(intent);
                finish();

            } else {
                // Error
                Toast.makeText(this,
                        "❌ Gagal membuat stand. Coba lagi.",
                        Toast.LENGTH_SHORT).show();

                // Reset button
                btnCreateStand.setEnabled(true);
                btnCreateStand.setText("🏪 Buat Stand");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating stand: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            // Reset button
            btnCreateStand.setEnabled(true);
            btnCreateStand.setText("🏪 Buat Stand");
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
    public void onBackPressed() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Batalkan Pembuatan Stand?")
                .setMessage("Anda belum membuat stand. Yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}