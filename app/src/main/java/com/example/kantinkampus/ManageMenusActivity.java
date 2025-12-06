package com.example.kantinkampus;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ManageMenusActivity extends AppCompatActivity implements MenuAdapterAdmin.OnMenuActionListener {
    private RecyclerView rvMenus;
    private LinearLayout layoutEmpty;
    private CardView btnAddMenu;
    private Spinner spinnerStand;

    private MenuAdapterAdmin adapter;
    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private List<Stand> standList;
    private int selectedStandId = -1;

    // Kategori options
    private String[] kategoriOptions = {"Makanan Berat", "Minuman", "Snack", "Dessert", "Lainnya"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_menus);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        rvMenus = findViewById(R.id.rvMenus);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnAddMenu = findViewById(R.id.btnAddMenu);
        spinnerStand = findViewById(R.id.spinnerStand);

        rvMenus.setLayoutManager(new GridLayoutManager(this, 2));

        setupStandSpinner();

        btnAddMenu.setOnClickListener(v -> {
            if (selectedStandId == -1) {
                Toast.makeText(this, "Pilih stand terlebih dahulu", Toast.LENGTH_SHORT).show();
            } else {
                showAddMenuDialog();
            }
        });
    }

    private void setupStandSpinner() {
        standList = dbHelper.getAllStands();

        List<String> standNames = new ArrayList<>();
        standNames.add("-- Pilih Stand --");
        for (Stand stand : standList) {
            standNames.add(stand.getNama());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, standNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStand.setAdapter(spinnerAdapter);

        spinnerStand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedStandId = standList.get(position - 1).getId();
                    loadMenus();
                } else {
                    selectedStandId = -1;
                    rvMenus.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStandId = -1;
            }
        });
    }

    private void loadMenus() {
        if (selectedStandId == -1) return;

        int userId = sessionManager.getUserId();
        List<Menu> menus = dbHelper.getMenuByStandId(selectedStandId, userId);

        if (menus.isEmpty()) {
            rvMenus.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvMenus.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new MenuAdapterAdmin(this, menus, this);
                rvMenus.setAdapter(adapter);
            } else {
                adapter.updateMenus(menus);
            }
        }
    }

    private void showAddMenuDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_menu, null);
        EditText etNama = dialogView.findViewById(R.id.etMenuNama);
        EditText etHarga = dialogView.findViewById(R.id.etMenuHarga);
        EditText etDeskripsi = dialogView.findViewById(R.id.etMenuDeskripsi);
        Spinner spinnerKategori = dialogView.findViewById(R.id.spinnerKategori);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rgStatus);

        // Setup kategori spinner
        ArrayAdapter<String> kategoriAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, kategoriOptions);
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(kategoriAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Tambah Menu Baru");
        builder.setView(dialogView);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nama = etNama.getText().toString().trim();
            String hargaStr = etHarga.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String kategori = spinnerKategori.getSelectedItem().toString();
            String status = rgStatus.getCheckedRadioButtonId() == R.id.rbAvailable ?
                    "available" : "unavailable";

            if (nama.isEmpty() || hargaStr.isEmpty()) {
                Toast.makeText(this, "Nama dan harga harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int harga = Integer.parseInt(hargaStr);

                long result = dbHelper.addMenu(selectedStandId, nama, harga, null,
                        deskripsi, kategori);

                if (result > 0) {
                    // Update status if unavailable
                    if (status.equals("unavailable")) {
                        dbHelper.updateMenu((int)result, nama, harga, null, deskripsi, kategori, status);
                    }

                    Toast.makeText(this, "✅ Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    loadMenus();
                } else {
                    Toast.makeText(this, "❌ Gagal menambahkan menu", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Harga harus berupa angka", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    @Override
    public void onToggleStatus(Menu menu) {
        String newStatus = menu.isAvailable() ? "unavailable" : "available";
        int result = dbHelper.updateMenu(menu.getId(), menu.getNama(), menu.getHarga(),
                menu.getImage(), menu.getDeskripsi(), menu.getKategori(), newStatus);

        if (result > 0) {
            String statusText = newStatus.equals("available") ? "Tersedia" : "Habis";
            Toast.makeText(this, "✅ Status diubah menjadi: " + statusText, Toast.LENGTH_SHORT).show();
            loadMenus();
        } else {
            Toast.makeText(this, "❌ Gagal mengubah status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditMenu(Menu menu) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_menu, null);
        EditText etNama = dialogView.findViewById(R.id.etMenuNama);
        EditText etHarga = dialogView.findViewById(R.id.etMenuHarga);
        EditText etDeskripsi = dialogView.findViewById(R.id.etMenuDeskripsi);
        Spinner spinnerKategori = dialogView.findViewById(R.id.spinnerKategori);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rgStatus);
        RadioButton rbAvailable = dialogView.findViewById(R.id.rbAvailable);
        RadioButton rbUnavailable = dialogView.findViewById(R.id.rbUnavailable);

        // Setup kategori spinner
        ArrayAdapter<String> kategoriAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, kategoriOptions);
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(kategoriAdapter);

        // Pre-fill with existing data
        etNama.setText(menu.getNama());
        etHarga.setText(String.valueOf(menu.getHarga()));
        etDeskripsi.setText(menu.getDeskripsi());

        // Set kategori
        if (menu.getKategori() != null) {
            for (int i = 0; i < kategoriOptions.length; i++) {
                if (kategoriOptions[i].equals(menu.getKategori())) {
                    spinnerKategori.setSelection(i);
                    break;
                }
            }
        }

        // Set status
        if (menu.isAvailable()) {
            rbAvailable.setChecked(true);
        } else {
            rbUnavailable.setChecked(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ Edit Menu");
        builder.setView(dialogView);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nama = etNama.getText().toString().trim();
            String hargaStr = etHarga.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String kategori = spinnerKategori.getSelectedItem().toString();
            String status = rgStatus.getCheckedRadioButtonId() == R.id.rbAvailable ?
                    "available" : "unavailable";

            if (nama.isEmpty() || hargaStr.isEmpty()) {
                Toast.makeText(this, "Nama dan harga harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int harga = Integer.parseInt(hargaStr);

                int result = dbHelper.updateMenu(menu.getId(), nama, harga, null,
                        deskripsi, kategori, status);

                if (result > 0) {
                    Toast.makeText(this, "✅ Menu berhasil diupdate", Toast.LENGTH_SHORT).show();
                    loadMenus();
                } else {
                    Toast.makeText(this, "❌ Gagal mengupdate menu", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Harga harus berupa angka", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    @Override
    public void onDeleteMenu(Menu menu) {
        new AlertDialog.Builder(this)
                .setTitle("🗑️ Hapus Menu")
                .setMessage("Apakah Anda yakin ingin menghapus menu \"" + menu.getNama() + "\"?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    int result = dbHelper.deleteMenu(menu.getId());

                    if (result > 0) {
                        Toast.makeText(this, "✅ Menu berhasil dihapus", Toast.LENGTH_SHORT).show();
                        loadMenus();
                    } else {
                        Toast.makeText(this, "❌ Gagal menghapus menu", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}