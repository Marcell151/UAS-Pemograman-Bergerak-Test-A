package com.example.kantinkampus;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViewCustomersActivity extends AppCompatActivity {
    private RecyclerView rvCustomers;
    private LinearLayout layoutEmpty;
    private TextView tvTotalCustomers;

    private CustomerAdapter adapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_customers);

        dbHelper = new DBHelper(this);

        rvCustomers = findViewById(R.id.rvCustomers);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotalCustomers = findViewById(R.id.tvTotalCustomers);

        rvCustomers.setLayoutManager(new LinearLayoutManager(this));

        loadCustomers();
    }

    private void loadCustomers() {
        List<User> customers = dbHelper.getAllCustomers();

        tvTotalCustomers.setText("Total: " + customers.size() + " customer");

        if (customers.isEmpty()) {
            rvCustomers.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvCustomers.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            adapter = new CustomerAdapter(this, customers);
            rvCustomers.setAdapter(adapter);
        }
    }
}