package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * NOTIFICATIONS ACTIVITY
 * Shows notifications for both seller and buyer
 */
public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private List<Notification> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifikasi");
        }

        // Initialize views
        rvNotifications = findViewById(R.id.rvNotifications);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Setup RecyclerView
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notifications = new ArrayList<>();

        // Load notifications
        loadNotifications();
    }

    private void loadNotifications() {
        int userId = sessionManager.getUserId();
        notifications = dbHelper.getUnreadNotifications(userId);

        if (notifications.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("🔔 Tidak ada notifikasi baru\n\nAnda akan menerima notifikasi untuk:\n• Pesanan baru (Penjual)\n• Status pesanan (Pembeli)\n• Verifikasi pembayaran\n• Dan lainnya");
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            // TODO: Create NotificationAdapter
            // For now, show count
            tvEmptyMessage.setText("Ada " + notifications.size() + " notifikasi");
            layoutEmpty.setVisibility(View.VISIBLE);
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