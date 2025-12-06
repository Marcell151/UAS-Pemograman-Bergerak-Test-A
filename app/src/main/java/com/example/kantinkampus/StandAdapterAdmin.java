package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StandAdapterAdmin extends RecyclerView.Adapter<StandAdapterAdmin.StandViewHolder> {
    private Context context;
    private List<Stand> standList;
    private OnStandActionListener listener;
    private String[] icons = {"🍱", "🥤", "🍟", "🍔", "🍜", "☕", "🍰", "🌮"}; // More icons

    public interface OnStandActionListener {
        void onEditStand(Stand stand);
        void onDeleteStand(Stand stand);
    }

    public StandAdapterAdmin(Context context, List<Stand> standList, OnStandActionListener listener) {
        this.context = context;
        this.standList = standList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stand_item_admin, parent, false);
        return new StandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StandViewHolder holder, int position) {
        Stand stand = standList.get(position);
        holder.tvStandNama.setText(stand.getNama());
        holder.tvStandDeskripsi.setText(stand.getDeskripsi());

        // Set icon based on position (cycling through icons array)
        if (position < icons.length) {
            holder.tvStandIcon.setText(icons[position]);
        } else {
            holder.tvStandIcon.setText(icons[position % icons.length]);
        }

        // Set click listeners
        holder.btnEditStand.setOnClickListener(v -> listener.onEditStand(stand));
        holder.btnDeleteStand.setOnClickListener(v -> listener.onDeleteStand(stand));
    }

    @Override
    public int getItemCount() {
        return standList.size();
    }

    public void updateStands(List<Stand> newStands) {
        this.standList = newStands;
        notifyDataSetChanged();
    }

    public static class StandViewHolder extends RecyclerView.ViewHolder {
        TextView tvStandNama, tvStandDeskripsi, tvStandIcon;
        CardView btnEditStand, btnDeleteStand;

        public StandViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStandNama = itemView.findViewById(R.id.tvStandNama);
            tvStandDeskripsi = itemView.findViewById(R.id.tvStandDeskripsi);
            tvStandIcon = itemView.findViewById(R.id.tvStandIcon);
            btnEditStand = itemView.findViewById(R.id.btnEditStand);
            btnDeleteStand = itemView.findViewById(R.id.btnDeleteStand);
        }
    }
}