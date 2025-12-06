package com.example.kantinkampus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StandAdapter extends RecyclerView.Adapter<StandAdapter.StandViewHolder> {
    private Context context;
    private List<Stand> standList;
    private String[] icons = {"🍱", "🥤", "🍟"}; // Icons for each stand

    public StandAdapter(Context context, List<Stand> standList) {
        this.context = context;
        this.standList = standList;
    }

    @NonNull
    @Override
    public StandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stand_item, parent, false);
        return new StandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StandViewHolder holder, int position) {
        Stand stand = standList.get(position);
        holder.tvStandNama.setText(stand.getNama());
        holder.tvStandDeskripsi.setText(stand.getDeskripsi());

        // Set icon based on position
        if (position < icons.length) {
            holder.tvStandIcon.setText(icons[position]);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MenuListActivity.class);
                intent.putExtra("stand_id", stand.getId());
                intent.putExtra("stand_nama", stand.getNama());
                intent.putExtra("stand_deskripsi", stand.getDeskripsi());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return standList.size();
    }

    public static class StandViewHolder extends RecyclerView.ViewHolder {
        TextView tvStandNama, tvStandDeskripsi, tvStandIcon;
        CardView cardView;

        public StandViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStandNama = itemView.findViewById(R.id.tvStandNama);
            tvStandDeskripsi = itemView.findViewById(R.id.tvStandDeskripsi);
            tvStandIcon = itemView.findViewById(R.id.tvStandIcon);
            cardView = (CardView) itemView;
        }
    }
}