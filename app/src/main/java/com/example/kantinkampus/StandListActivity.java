package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * STAND LIST ACTIVITY - Browse All Stands
 * Shows all available stands in grid layout
 */
public class StandListActivity extends AppCompatActivity {
    private static final String TAG = "StandListActivity";

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    private RecyclerView rvStands;
    private TextView tvEmptyState;

    private StandAdapter standAdapter;
    private List<Stand> standList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stand_list);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pilih Stand");
        }

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Initialize views
        initViews();

        // Load stands
        loadStands();
    }

    private void initViews() {
        rvStands = findViewById(R.id.rvStands);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Setup RecyclerView with Grid Layout (2 columns)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvStands.setLayoutManager(gridLayoutManager);
        rvStands.setHasFixedSize(true);
    }

    private void loadStands() {
        try {
            standList = dbHelper.getAllStands();

            if (standList.isEmpty()) {
                rvStands.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvStands.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);

                // Setup adapter
                standAdapter = new StandAdapter(this, standList, stand -> {
                    // Navigate to menu list
                    Intent intent = new Intent(StandListActivity.this, MenuListActivity.class);
                    intent.putExtra("stand_id", stand.getId());
                    intent.putExtra("stand_name", stand.getNama());
                    startActivity(intent);
                });

                rvStands.setAdapter(standAdapter);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading stands: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        loadStands(); // Refresh when returning
    }
}