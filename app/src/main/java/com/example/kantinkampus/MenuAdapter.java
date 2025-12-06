package com.example.kantinkampus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
    private Context context;
    private List<Menu> menuList;
    private OnMenuClickListener listener;

    public interface OnMenuClickListener {
        void onAddToCart(Menu menu);
    }

    public MenuAdapter(Context context, List<Menu> menuList, OnMenuClickListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Menu menu = menuList.get(position);
        holder.tvMenuNama.setText(menu.getNama());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String hargaFormatted = formatter.format(menu.getHarga());
        holder.tvMenuHarga.setText(hargaFormatted);

        // Show rating if available
        if (menu.getTotalReviews() > 0) {
            String ratingText = String.format(Locale.getDefault(), "⭐ %.1f (%d)",
                    menu.getAverageRating(), menu.getTotalReviews());
            holder.tvMenuNama.setText(menu.getNama() + "\n" + ratingText);
        }

        // Add to cart button
        holder.btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAddToCart(menu);
            }
        });

        // Click on item to view detail
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MenuDetailActivity.class);
                intent.putExtra("menu_id", menu.getId());
                context.startActivity(intent);
            }
        });

        // Long press to add to favorites
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // This will be handled in MenuDetailActivity
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvMenuNama, tvMenuHarga, btnTambah;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMenuNama = itemView.findViewById(R.id.tvMenuNama);
            tvMenuHarga = itemView.findViewById(R.id.tvMenuHarga);
            btnTambah = itemView.findViewById(R.id.btnTambah);
        }
    }
}