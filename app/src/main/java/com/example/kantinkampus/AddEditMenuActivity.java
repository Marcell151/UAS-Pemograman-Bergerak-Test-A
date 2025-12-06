package com.example.kantinkampus;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class AddEditMenuActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etMenuName, etMenuPrice, etMenuDescription;
    private Spinner spinnerStand, spinnerCategory;
    private CheckBox cbAvailable;
    private TextView btnSaveMenu;
    private CardView btnBack;

    private int menuId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_menu);

        dbHelper = new DBHelper(this);

        // Initialize views
        etMenuName = findViewById(R.id.etMenuName);
        etMenuPrice = findViewById(R.id.etMenuPrice);
        etMenuDescription = findViewById(R.id.etMenuDescription);
        spinnerStand = findViewById(R.id.spinnerStand);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbAvailable = findViewById(R.id.cbAvailable);
        btnSaveMenu = findViewById(R.id.btnSaveMenu);
        btnBack = findViewById(R.id.btnBack);

        // Setup spinners
        setupStandSpinner();
        setupCategorySpinner();

        // Check if edit mode
        menuId = getIntent().getIntExtra("menu_id", -1);
        if (menuId != -1) {
            isEditMode = true;
            loadMenuData();
            btnSaveMenu.setText("💾 Update Menu");
        }

        // Setup buttons
        btnSaveMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMenu();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupStandSpinner() {
        List<Stand> stands = dbHelper.getAllStands();
        List<String> standNames = new ArrayList<>();

        for (Stand stand : stands) {
            standNames.add(stand.getNama());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                standNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStand.setAdapter(adapter);
    }

    private void setupCategorySpinner() {
        String[] categories = {
                "Makanan Berat",
                "Minuman",
                "Snack",
                "Dessert",
                "Lainnya"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void loadMenuData() {
        Menu menu = dbHelper.getMenuById(menuId);
        if (menu != null) {
            etMenuName.setText(menu.getNama());
            etMenuPrice.setText(String.valueOf(menu.getHarga()));
            etMenuDescription.setText(menu.getDeskripsi());
            cbAvailable.setChecked(menu.isAvailable());

            // Set stand spinner
            List<Stand> stands = dbHelper.getAllStands();
            for (int i = 0; i < stands.size(); i++) {
                if (stands.get(i).getId() == menu.getStandId()) {
                    spinnerStand.setSelection(i);
                    break;
                }
            }

            // Set category spinner
            String[] categories = {
                    "Makanan Berat",
                    "Minuman",
                    "Snack",
                    "Dessert",
                    "Lainnya"
            };
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(menu.getKategori())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveMenu() {
        String menuName = etMenuName.getText().toString().trim();
        String priceStr = etMenuPrice.getText().toString().trim();
        String description = etMenuDescription.getText().toString().trim();

        // Validation
        if (menuName.isEmpty()) {
            etMenuName.setError("Nama menu tidak boleh kosong");
            etMenuName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etMenuPrice.setError("Harga tidak boleh kosong");
            etMenuPrice.requestFocus();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
            if (price <= 0) {
                etMenuPrice.setError("Harga harus lebih dari 0");
                etMenuPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etMenuPrice.setError("Harga tidak valid");
            etMenuPrice.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etMenuDescription.setError("Deskripsi tidak boleh kosong");
            etMenuDescription.requestFocus();
            return;
        }

        // Get selected stand
        int standPosition = spinnerStand.getSelectedItemPosition();
        List<Stand> stands = dbHelper.getAllStands();
        int standId = stands.get(standPosition).getId();

        // Get selected category
        String category = spinnerCategory.getSelectedItem().toString();

        // Get availability
        boolean available = cbAvailable.isChecked();

        // Create or update menu
        Menu menu = new Menu();
        if (isEditMode) {
            menu.setId(menuId);
        }
        menu.setStandId(standId);
        menu.setNama(menuName);
        menu.setHarga(price);
        menu.setDeskripsi(description);
        menu.setKategori(category);
        menu.setAvailable(available);

        if (isEditMode) {
            dbHelper.updateMenu(menu);
            Toast.makeText(this, "✅ Menu berhasil diupdate!", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addMenu(menu);
            Toast.makeText(this, "✅ Menu berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}