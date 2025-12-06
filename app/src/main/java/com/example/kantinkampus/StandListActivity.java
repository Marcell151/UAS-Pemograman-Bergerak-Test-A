package com.example.kantinkampus;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kantinkampus.R;

import java.util.List;

public class StandListActivity extends AppCompatActivity {
    private RecyclerView rvStand;
    private StandAdapter standAdapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standlist);

        dbHelper = new DBHelper(this);
        rvStand = findViewById(R.id.rvStand);
        rvStand.setLayoutManager(new LinearLayoutManager(this));

        loadStands();
    }

    private void loadStands() {
        List<Stand> stands = dbHelper.getAllStands();
        standAdapter = new StandAdapter(this, stands);
        rvStand.setAdapter(standAdapter);
    }
}