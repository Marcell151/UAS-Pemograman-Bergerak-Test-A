package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * STAND ADAPTER - For Buyer's Stand Browsing
 * Shows stand list in grid layout
 */
public class StandAdapter extends RecyclerView.Adapter<StandAdapter.StandViewHolder> {

    private Context context;
    private List<Stand> standList;
    private OnStandClickListener listener;

    public interface OnStandClickListener {
        void onStandClick(Stand stand);
    }

    public StandAdapter(Context context, List<Stand> standList, OnStandClickListener listener) {
        this.context = context;
        this.standList = standList;
        this.listener = listener;
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

        // Set stand info
        holder.tvStandName.setText(stand.getNama());

        // Set description or hide
        if (stand.getDeskripsi() != null && !stand.getDeskripsi().isEmpty()) {
            holder.tvStandDescription.setText(stand.getDeskripsi());
            holder.tvStandDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvStandDescription.setVisibility(View.GONE);
        }

        // Set image (placeholder for now)
        holder.ivStandImage.setImageResource(R.drawable.ic_book_placeholder);

        // Click listener
        holder.cardStand.setOnClickListener(v -> listener.onStandClick(stand));
    }

    @Override
    public int getItemCount() {
        return standList.size();
    }

    public void updateList(List<Stand> newList) {
        this.standList = newList;
        notifyDataSetChanged();
    }

    public static class StandViewHolder extends RecyclerView.ViewHolder {
        CardView cardStand;
        ImageView ivStandImage;
        TextView tvStandName, tvStandDescription;

        public StandViewHolder(@NonNull View itemView) {
            super(itemView);

            cardStand = itemView.findViewById(R.id.cardStand);
            ivStandImage = itemView.findViewById(R.id.ivStandImage);
            tvStandName = itemView.findViewById(R.id.tvStandName);
            tvStandDescription = itemView.findViewById(R.id.tvStandDescription);
        }
    }
}