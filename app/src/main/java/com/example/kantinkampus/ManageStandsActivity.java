package com.example.kantinkampus;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ManageStandsActivity extends AppCompatActivity implements StandAdapterAdmin.OnStandActionListener {
    private RecyclerView rvStands;
    private LinearLayout layoutEmpty;
    private CardView btnAddStand;

    private StandAdapterAdmin adapter;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_stands);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        rvStands = findViewById(R.id.rvStands);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnAddStand = findViewById(R.id.btnAddStand);

        rvStands.setLayoutManager(new LinearLayoutManager(this));

        btnAddStand.setOnClickListener(v -> showAddStandDialog());

        loadStands();
    }

    private void loadStands() {
        List<Stand> stands = dbHelper.getAllStands();

        if (stands.isEmpty()) {
            rvStands.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvStands.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            if (adapter == null) {
                adapter = new StandAdapterAdmin(this, stands, this);
                rvStands.setAdapter(adapter);
            } else {
                adapter.updateStands(stands);
            }
        }
    }

    private void showAddStandDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_stand, null);
        EditText etNama = dialogView.findViewById(R.id.etStandNama);
        EditText etDeskripsi = dialogView.findViewById(R.id.etStandDeskripsi);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Tambah Stand Baru");
        builder.setView(dialogView);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nama = etNama.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();

            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama stand tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            int ownerId = sessionManager.getUserId();
            long result = dbHelper.addStand(nama, deskripsi, null, ownerId);

            if (result > 0) {
                Toast.makeText(this, "✅ Stand berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                loadStands();
            } else {
                Toast.makeText(this, "❌ Gagal menambahkan stand", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    @Override
    public void onEditStand(Stand stand) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_stand, null);
        EditText etNama = dialogView.findViewById(R.id.etStandNama);
        EditText etDeskripsi = dialogView.findViewById(R.id.etStandDeskripsi);

        // Pre-fill with existing data
        etNama.setText(stand.getNama());
        etDeskripsi.setText(stand.getDeskripsi());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ Edit Stand");
        builder.setView(dialogView);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nama = etNama.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();

            if (nama.isEmpty()) {
                Toast.makeText(this, "Nama stand tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            int result = dbHelper.updateStand(stand.getId(), nama, deskripsi, null);

            if (result > 0) {
                Toast.makeText(this, "✅ Stand berhasil diupdate", Toast.LENGTH_SHORT).show();
                loadStands();
            } else {
                Toast.makeText(this, "❌ Gagal mengupdate stand", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    @Override
    public void onDeleteStand(Stand stand) {
        new AlertDialog.Builder(this)
                .setTitle("🗑️ Hapus Stand")
                .setMessage("Apakah Anda yakin ingin menghapus stand \"" + stand.getNama() + "\"?\n\n" +
                        "⚠️ Semua menu di stand ini juga akan terhapus!")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    int result = dbHelper.deleteStand(stand.getId());

                    if (result > 0) {
                        Toast.makeText(this, "✅ Stand berhasil dihapus", Toast.LENGTH_SHORT).show();
                        loadStands();
                    } else {
                        Toast.makeText(this, "❌ Gagal menghapus stand", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}