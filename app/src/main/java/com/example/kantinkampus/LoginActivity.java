package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * LOGIN ACTIVITY - Updated for Dual Role System
 * Supports login as Seller or Buyer
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private TextView btnLogin, tvRegister;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Initialize
            sessionManager = new SessionManager(this);
            dbHelper = new DBHelper(this);

            // Check if already logged in
            if (sessionManager.isLoggedIn()) {
                redirectBasedOnRole();
                return;
            }

            setContentView(R.layout.activity_login);

            // Initialize views
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            tvRegister = findViewById(R.id.tvRegister);

            // Set listeners
            btnLogin.setOnClickListener(v -> attemptLogin());
            tvRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void attemptLogin() {
        try {
            // Reset errors
            etEmail.setError(null);
            etPassword.setError(null);

            // Get values
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            boolean cancel = false;
            View focusView = null;

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

            if (cancel) {
                focusView.requestFocus();
            } else {
                performLogin(email, password);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in attemptLogin: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performLogin(String email, String password) {
        try {
            User user = dbHelper.loginUser(email, password);

            if (user != null) {
                // Save session
                sessionManager.createLoginSession(user);

                // Show success message with role
                String roleText = user.isSeller() ? "Penjual" :
                        user.isMahasiswa() ? "Mahasiswa" :
                                user.isDosen() ? "Dosen" : "User";

                Toast.makeText(this,
                        "✅ Login berhasil!\nSelamat datang, " + user.getName() + " (" + roleText + ")",
                        Toast.LENGTH_SHORT).show();

                // Redirect based on role
                redirectBasedOnRole();
                finish();
            } else {
                Toast.makeText(this, "❌ Email atau password salah!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in performLogin: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectBasedOnRole() {
        try {
            Intent intent;
            if (sessionManager.isSeller()) {
                // Redirect to Seller Dashboard
                intent = new Intent(LoginActivity.this, SellerDashboardActivity.class);
            } else {
                // Redirect to Buyer Home (MainActivity)
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error in redirect: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    @Override
    public void onBackPressed() {
        // Exit app when back pressed on login screen
        moveTaskToBack(true);
    }
}