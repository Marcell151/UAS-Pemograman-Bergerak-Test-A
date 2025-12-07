package com.example.kantinkampus;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * ADD REVIEW ACTIVITY
 * Allows buyer to rate and review a menu after order completion
 */
public class AddReviewActivity extends AppCompatActivity {
    private TextView tvMenuName;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private int menuId;
    private int orderId;
    private String menuName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        // Get data from intent
        menuId = getIntent().getIntExtra("menu_id", -1);
        orderId = getIntent().getIntExtra("order_id", -1);
        menuName = getIntent().getStringExtra("menu_name");

        if (menuId == -1) {
            Toast.makeText(this, "Error: Menu tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Berikan Ulasan");
        }

        // Initialize views
        tvMenuName = findViewById(R.id.tvMenuName);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Set menu name
        tvMenuName.setText(menuName != null ? menuName : "Menu");

        // Default rating
        ratingBar.setRating(5);

        // Submit button
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Pilih rating terlebih dahulu (1-5 bintang)", Toast.LENGTH_SHORT).show();
            return;
        }

        int buyerId = sessionManager.getUserId();

        long result = dbHelper.addReview(buyerId, menuId, orderId, (int) rating, comment);

        if (result > 0) {
            Toast.makeText(this, "✅ Ulasan berhasil dikirim!\nTerima kasih atas feedback Anda.",
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Gagal mengirim ulasan. Coba lagi.", Toast.LENGTH_SHORT).show();
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
}