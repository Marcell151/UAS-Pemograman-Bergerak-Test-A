package com.example.kantinkampus;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AddReviewActivity extends AppCompatActivity {
    private int menuId;
    private String menuName;
    private int selectedRating = 0;

    private TextView tvMenuName, tvRatingValue, btnSubmitReview;
    private RatingBar ratingBar;
    private EditText etUserName, etComment;
    private CardView btnBack;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        dbHelper = new DBHelper(this);

        // Get data from intent
        menuId = getIntent().getIntExtra("menu_id", -1);
        menuName = getIntent().getStringExtra("menu_name");

        if (menuId == -1) {
            Toast.makeText(this, "Error: Menu tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvMenuName = findViewById(R.id.tvMenuName);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        ratingBar = findViewById(R.id.ratingBar);
        etUserName = findViewById(R.id.etUserName);
        etComment = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnBack = findViewById(R.id.btnBack);

        // Set menu name
        tvMenuName.setText(menuName);

        // Setup rating bar
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                selectedRating = (int) rating;
                tvRatingValue.setText(selectedRating + " / 5");

                // Update rating text
                String ratingText = "";
                switch (selectedRating) {
                    case 1:
                        ratingText = "Sangat Buruk";
                        break;
                    case 2:
                        ratingText = "Buruk";
                        break;
                    case 3:
                        ratingText = "Cukup";
                        break;
                    case 4:
                        ratingText = "Bagus";
                        break;
                    case 5:
                        ratingText = "Sangat Bagus";
                        break;
                }
                tvRatingValue.setText(selectedRating + " / 5 - " + ratingText);
            }
        });

        // Setup submit button
        btnSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview();
            }
        });

        // Setup back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void submitReview() {
        String userName = etUserName.getText().toString().trim();
        String comment = etComment.getText().toString().trim();

        // Validation
        if (userName.isEmpty()) {
            etUserName.setError("Nama tidak boleh kosong");
            etUserName.requestFocus();
            return;
        }

        if (selectedRating == 0) {
            Toast.makeText(this, "Silakan pilih rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            etComment.setError("Komentar tidak boleh kosong");
            etComment.requestFocus();
            return;
        }

        // Save review to database
        dbHelper.addReview(menuId, userName, selectedRating, comment);

        Toast.makeText(this, "✅ Review berhasil ditambahkan!", Toast.LENGTH_SHORT).show();

        // Return to previous activity
        finish();
    }
}