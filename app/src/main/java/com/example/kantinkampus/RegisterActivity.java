package com.example.kantinkampus;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * REGISTER ACTIVITY - Dual Role System
 * Support registration as Seller or Buyer (Mahasiswa/Dosen/Staff)
 */
public class RegisterActivity extends AppCompatActivity {
    private RadioGroup rgRole, rgBuyerType;
    private RadioButton rbSeller, rbBuyer, rbMahasiswa, rbDosen, rbStaff;
    private EditText etName, etEmail, etPassword, etPhone, etIdNumber;
    private TextView btnRegister, tvLogin, tvIdNumberLabel, tvIdNumberHint;
    private LinearLayout layoutBuyerType;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DBHelper(this);

        // Initialize views
        initViews();

        // Set default state
        rbBuyer.setChecked(true);
        rbMahasiswa.setChecked(true);
        layoutBuyerType.setVisibility(View.VISIBLE);
        updateIdNumberLabel();

        // Setup listeners
        setupRoleListener();
        setupBuyerTypeListener();

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rgRole = findViewById(R.id.rgRole);
        rgBuyerType = findViewById(R.id.rgBuyerType);
        rbSeller = findViewById(R.id.rbSeller);
        rbBuyer = findViewById(R.id.rbBuyer);
        rbMahasiswa = findViewById(R.id.rbMahasiswa);
        rbDosen = findViewById(R.id.rbDosen);
        rbStaff = findViewById(R.id.rbStaff);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etIdNumber = findViewById(R.id.etIdNumber);

        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvIdNumberLabel = findViewById(R.id.tvIdNumberLabel);
        tvIdNumberHint = findViewById(R.id.tvIdNumberHint);

        layoutBuyerType = findViewById(R.id.layoutBuyerType);
    }

    private void setupRoleListener() {
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSeller) {
                // Seller: Hide buyer type, show Kartu Usaha field
                layoutBuyerType.setVisibility(View.GONE);
                rgBuyerType.clearCheck();
                tvIdNumberLabel.setText("Nomor Kartu Usaha Kerjasama Kampus");
                tvIdNumberHint.setText("💼 Contoh: KARTU-001, KU-2024-123");
                etIdNumber.setHint("Masukkan nomor kartu usaha");
            } else if (checkedId == R.id.rbBuyer) {
                // Buyer: Show buyer type selection
                layoutBuyerType.setVisibility(View.VISIBLE);
                rbMahasiswa.setChecked(true);
                updateIdNumberLabel();
            }
        });
    }

    private void setupBuyerTypeListener() {
        rgBuyerType.setOnCheckedChangeListener((group, checkedId) -> {
            if (rbBuyer.isChecked()) {
                updateIdNumberLabel();
            }
        });
    }

    private void updateIdNumberLabel() {
        if (rbMahasiswa.isChecked()) {
            tvIdNumberLabel.setText("NIM (Nomor Induk Mahasiswa)");
            tvIdNumberHint.setText("🎓 Contoh: 123456789");
            etIdNumber.setHint("Masukkan NIM");
        } else if (rbDosen.isChecked()) {
            tvIdNumberLabel.setText("NIP (Nomor Induk Pegawai - Dosen)");
            tvIdNumberHint.setText("👨‍🏫 Contoh: 198501012010012001");
            etIdNumber.setHint("Masukkan NIP");
        } else if (rbStaff.isChecked()) {
            tvIdNumberLabel.setText("NIP (Nomor Induk Pegawai - Staff)");
            tvIdNumberHint.setText("👔 Contoh: 199001012015012001");
            etIdNumber.setHint("Masukkan NIP");
        }
    }

    private void attemptRegister() {
        // Reset errors
        etName.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etPhone.setError(null);
        etIdNumber.setError(null);

        // Get values
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String idNumber = etIdNumber.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Determine role and type
        String role = rbSeller.isChecked() ? "seller" : "buyer";
        String type = null;

        if (rbBuyer.isChecked()) {
            if (rbMahasiswa.isChecked()) {
                type = "mahasiswa";
            } else if (rbDosen.isChecked()) {
                type = "dosen";
            } else if (rbStaff.isChecked()) {
                type = "staff";
            }
        }

        // Validate ID Number
        if (TextUtils.isEmpty(idNumber)) {
            etIdNumber.setError("Nomor identitas tidak boleh kosong");
            focusView = etIdNumber;
            cancel = true;
        } else {
            // Validate based on type
            if (role.equals("seller")) {
                // Kartu Usaha validation
                if (idNumber.length() < 5) {
                    etIdNumber.setError("Nomor kartu usaha tidak valid (minimal 5 karakter)");
                    focusView = etIdNumber;
                    cancel = true;
                }
            } else if (type != null) {
                if (type.equals("mahasiswa")) {
                    // NIM validation (usually 9 digits)
                    if (idNumber.length() < 8 || !idNumber.matches("\\d+")) {
                        etIdNumber.setError("NIM tidak valid (minimal 8 digit angka)");
                        focusView = etIdNumber;
                        cancel = true;
                    }
                } else if (type.equals("dosen") || type.equals("staff")) {
                    // NIP validation (usually 18 digits)
                    if (idNumber.length() < 18 || !idNumber.matches("\\d+")) {
                        etIdNumber.setError("NIP tidak valid (harus 18 digit angka)");
                        focusView = etIdNumber;
                        cancel = true;
                    }
                }
            }
        }

        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("No. HP tidak boleh kosong");
            focusView = etPhone;
            cancel = true;
        } else if (phone.length() < 10) {
            etPhone.setError("No. HP minimal 10 digit");
            focusView = etPhone;
            cancel = true;
        } else if (!phone.matches("\\d+")) {
            etPhone.setError("No. HP harus berupa angka");
            focusView = etPhone;
            cancel = true;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            focusView = etPassword;
            cancel = true;
        } else if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            focusView = etPassword;
            cancel = true;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            focusView = etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            etEmail.setError("Format email tidak valid");
            focusView = etEmail;
            cancel = true;
        }

        // Validate name
        if (TextUtils.isEmpty(name)) {
            etName.setError("Nama tidak boleh kosong");
            focusView = etName;
            cancel = true;
        } else if (name.length() < 3) {
            etName.setError("Nama minimal 3 karakter");
            focusView = etName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            performRegister(email, password, name, role, phone, idNumber, type);
        }
    }

    private void performRegister(String email, String password, String name,
                                 String role, String phone, String idNumber, String type) {
        long result = dbHelper.registerUser(email, password, name, role, phone, idNumber, type);

        if (result > 0) {
            String roleText;
            if (role.equals("seller")) {
                roleText = "Penjual";
            } else if (type != null) {
                switch (type) {
                    case "mahasiswa":
                        roleText = "Mahasiswa";
                        break;
                    case "dosen":
                        roleText = "Dosen";
                        break;
                    case "staff":
                        roleText = "Staff";
                        break;
                    default:
                        roleText = "Pembeli";
                }
            } else {
                roleText = "User";
            }

            Toast.makeText(this,
                    "✅ Registrasi berhasil sebagai " + roleText + "!\n" +
                            "Silakan login dengan akun Anda.",
                    Toast.LENGTH_LONG).show();

            // Redirect to login
            finish();
        } else if (result == -1) {
            Toast.makeText(this,
                    "❌ Email sudah terdaftar!\nGunakan email lain atau login.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    "❌ Terjadi kesalahan saat registrasi. Coba lagi.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }
}