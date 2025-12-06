package com.example.kantinkampus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

/**
 * SELLER MANAGE MENUS ACTIVITY
 * Complete menu management: Add, Edit, Delete, Toggle availability
 */
public class SellerManageMenusActivity extends AppCompatActivity {
    private static final String TAG = "ManageMenus";

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private RecyclerView rvMenus;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddMenu;

    private MenuAdapter menuAdapter;
    private List<Menu> menuList;

    private int sellerId;
    private int standId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_manage_menus);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kelola Menu");
        }

        // Initialize
        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);
        sellerId = sessionManager.getUserId();

        // Get stand ID
        standId = getIntent().getIntExtra("stand_id", -1);

        if (standId == -1) {
            // Get from seller's stand
            Stand stand = dbHelper.getStandBySeller(sellerId);
            if (stand != null) {
                standId = stand.getId();
            } else {
                Toast.makeText(this, "⚠️ Stand tidak ditemukan!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Initialize views
        initViews();

        // Load menus
        loadMenus();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        rvMenus = findViewById(R.id.rvMenus);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddMenu = findViewById(R.id.fabAddMenu);

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

                // Setup adapter with listeners
                menuAdapter = new MenuAdapter(this, menuList, new MenuAdapter.OnMenuClickListener() {
                    @Override
                    public void onMenuClick(Menu menu) {
                        showMenuDetailsDialog(menu);
                    }

                    @Override
                    public void onEditClick(Menu menu) {
                        showEditMenuDialog(menu);
                    }

                    @Override
                    public void onDeleteClick(Menu menu) {
                        showDeleteConfirmation(menu);
                    }

                    @Override
                    public void onToggleStatus(Menu menu) {
                        toggleMenuStatus(menu);
                    }
                });

                rvMenus.setAdapter(menuAdapter);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading menus: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        fabAddMenu.setOnClickListener(v -> showAddMenuDialog());
    }

    private void showAddMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Tambah Menu Baru");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_menu, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etMenuName);
        EditText etPrice = dialogView.findViewById(R.id.etMenuPrice);
        EditText etDescription = dialogView.findViewById(R.id.etMenuDescription);
        RadioGroup rgCategory = dialogView.findViewById(R.id.rgCategory);

        builder.setPositiveButton("Tambah", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "❌ Nama dan harga harus diisi!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int price;
            try {
                price = Integer.parseInt(priceStr);
                if (price <= 0) {
                    Toast.makeText(this, "❌ Harga harus lebih dari 0!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "❌ Harga tidak valid!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Get category
            int selectedId = rgCategory.getCheckedRadioButtonId();
            RadioButton rbCategory = dialogView.findViewById(selectedId);
            String category = rbCategory != null ? rbCategory.getText().toString() : "Lainnya";

            // Add menu
            long result = dbHelper.addMenu(standId, name, price, null, description, category);

            if (result > 0) {
                Toast.makeText(this, "✅ Menu berhasil ditambahkan!",
                        Toast.LENGTH_SHORT).show();
                loadMenus(); // Refresh
            } else {
                Toast.makeText(this, "❌ Gagal menambahkan menu!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showEditMenuDialog(Menu menu) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ Edit Menu");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_menu, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etMenuName);
        EditText etPrice = dialogView.findViewById(R.id.etMenuPrice);
        EditText etDescription = dialogView.findViewById(R.id.etMenuDescription);
        RadioGroup rgCategory = dialogView.findViewById(R.id.rgCategory);

        // Set current values
        etName.setText(menu.getNama());
        etPrice.setText(String.valueOf(menu.getHarga()));
        etDescription.setText(menu.getDeskripsi());

        // Set category
        for (int i = 0; i < rgCategory.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgCategory.getChildAt(i);
            if (rb.getText().toString().equals(menu.getKategori())) {
                rb.setChecked(true);
                break;
            }
        }

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "❌ Nama dan harga harus diisi!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int price;
            try {
                price = Integer.parseInt(priceStr);
                if (price <= 0) {
                    Toast.makeText(this, "❌ Harga harus lebih dari 0!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "❌ Harga tidak valid!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Get category
            int selectedId = rgCategory.getCheckedRadioButtonId();
            RadioButton rbCategory = dialogView.findViewById(selectedId);
            String category = rbCategory != null ? rbCategory.getText().toString() : menu.getKategori();

            // Update menu
            int result = dbHelper.updateMenu(menu.getId(), name, price, null,
                    description, category, menu.getStatus());

            if (result > 0) {
                Toast.makeText(this, "✅ Menu berhasil diupdate!",
                        Toast.LENGTH_SHORT).show();
                loadMenus(); // Refresh
            } else {
                Toast.makeText(this, "❌ Gagal update menu!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showDeleteConfirmation(Menu menu) {
        new AlertDialog.Builder(this)
                .setTitle("🗑️ Hapus Menu")
                .setMessage("Yakin ingin menghapus menu \"" + menu.getNama() + "\"?\n\n" +
                        "Menu yang sudah dihapus tidak dapat dikembalikan.")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    int result = dbHelper.deleteMenu(menu.getId());

                    if (result > 0) {
                        Toast.makeText(this, "✅ Menu berhasil dihapus!",
                                Toast.LENGTH_SHORT).show();
                        loadMenus(); // Refresh
                    } else {
                        Toast.makeText(this, "❌ Gagal menghapus menu!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void toggleMenuStatus(Menu menu) {
        String newStatus = menu.isAvailable() ? "unavailable" : "available";

        int result = dbHelper.updateMenu(menu.getId(), menu.getNama(),
                menu.getHarga(), menu.getImage(), menu.getDeskripsi(),
                menu.getKategori(), newStatus);

        if (result > 0) {
            String message = newStatus.equals("available") ?
                    "✅ Menu tersedia untuk dijual" :
                    "⚠️ Menu tidak tersedia sementara";

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            loadMenus(); // Refresh
        } else {
            Toast.makeText(this, "❌ Gagal update status!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showMenuDetailsDialog(Menu menu) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(menu.getNama());

        String details = "💰 Harga: " + menu.getFormattedPrice() + "\n\n" +
                "📝 Deskripsi: " + (menu.getDeskripsi() != null ? menu.getDeskripsi() : "-") + "\n\n" +
                "🏷️ Kategori: " + menu.getKategori() + "\n\n" +
                "📊 Status: " + (menu.isAvailable() ? "✅ Tersedia" : "⚠️ Tidak Tersedia") + "\n\n" +
                "⭐ Rating: " + String.format("%.1f", menu.getAverageRating()) +
                " (" + menu.getTotalReviews() + " review)";

        builder.setMessage(details);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
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