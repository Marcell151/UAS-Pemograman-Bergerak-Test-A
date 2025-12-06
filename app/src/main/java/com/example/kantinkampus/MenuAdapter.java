package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * MENU ADAPTER - For Seller's Menu Management
 * Shows menu list with edit, delete, toggle status
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private Context context;
    private List<Menu> menuList;
    private OnMenuClickListener listener;

    public interface OnMenuClickListener {
        void onMenuClick(Menu menu);
        void onEditClick(Menu menu);
        void onDeleteClick(Menu menu);
        void onToggleStatus(Menu menu);
    }

    public MenuAdapter(Context context, List<Menu> menuList, OnMenuClickListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item_seller, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Menu menu = menuList.get(position);

        // Set menu info
        holder.tvMenuName.setText(menu.getNama());
        holder.tvMenuPrice.setText(menu.getFormattedPrice());
        holder.tvMenuCategory.setText(menu.getKategori());

        // Set description or hide
        if (menu.getDeskripsi() != null && !menu.getDeskripsi().isEmpty()) {
            holder.tvMenuDescription.setText(menu.getDeskripsi());
            holder.tvMenuDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvMenuDescription.setVisibility(View.GONE);
        }

        // Set rating & reviews
        if (menu.getTotalReviews() > 0) {
            holder.tvRating.setText(String.format("⭐ %.1f (%d)",
                    menu.getAverageRating(), menu.getTotalReviews()));
            holder.tvRating.setVisibility(View.VISIBLE);
        } else {
            holder.tvRating.setVisibility(View.GONE);
        }

        // Set availability switch
        holder.switchAvailable.setChecked(menu.isAvailable());
        holder.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Only if user clicked, not programmatic
                listener.onToggleStatus(menu);
            }
        });

        // Set status indicator
        if (menu.isAvailable()) {
            holder.tvStatus.setText("✅ Tersedia");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.success));
            holder.cardMenu.setCardBackgroundColor(context.getResources().getColor(R.color.white));
        } else {
            holder.tvStatus.setText("⚠️ Tidak Tersedia");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.text_gray));
            holder.cardMenu.setCardBackgroundColor(context.getResources().getColor(R.color.light_gray));
        }

        // Set image (placeholder for now)
        holder.ivMenuImage.setImageResource(R.drawable.ic_book_placeholder);

        // Click listeners
        holder.cardMenu.setOnClickListener(v -> listener.onMenuClick(menu));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(menu));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(menu));
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public void updateList(List<Menu> newList) {
        this.menuList = newList;
        notifyDataSetChanged();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        CardView cardMenu;
        ImageView ivMenuImage;
        TextView tvMenuName, tvMenuPrice, tvMenuCategory, tvMenuDescription;
        TextView tvStatus, tvRating;
        Switch switchAvailable;
        TextView btnEdit, btnDelete;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);

            cardMenu = itemView.findViewById(R.id.cardMenu);
            ivMenuImage = itemView.findViewById(R.id.ivMenuImage);
            tvMenuName = itemView.findViewById(R.id.tvMenuName);
            tvMenuPrice = itemView.findViewById(R.id.tvMenuPrice);
            tvMenuCategory = itemView.findViewById(R.id.tvMenuCategory);
            tvMenuDescription = itemView.findViewById(R.id.tvMenuDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRating = itemView.findViewById(R.id.tvRating);
            switchAvailable = itemView.findViewById(R.id.switchAvailable);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}