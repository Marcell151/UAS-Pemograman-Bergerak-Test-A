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
 * MENU ADAPTER FOR BUYER
 * Shows menu list with add to cart and favorite buttons
 */
public class MenuAdapterBuyer extends RecyclerView.Adapter<MenuAdapterBuyer.MenuViewHolder> {

    private Context context;
    private List<Menu> menuList;
    private OnMenuClickListener listener;
    private DBHelper dbHelper;
    private SessionManager sessionManager;

    public interface OnMenuClickListener {
        void onMenuClick(Menu menu);
        void onAddToCart(Menu menu);
        void onToggleFavorite(Menu menu);
    }

    public MenuAdapterBuyer(Context context, List<Menu> menuList, OnMenuClickListener listener) {
        this.context = context;
        this.menuList = menuList;
        this.listener = listener;
        this.dbHelper = new DBHelper(context);
        this.sessionManager = new SessionManager(context);
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
        int buyerId = sessionManager.getUserId();

        // Set menu info
        holder.tvMenuName.setText(menu.getNama());
        holder.tvMenuPrice.setText(menu.getFormattedPrice());
        holder.tvMenuCategory.setText("🏷️ " + menu.getKategori());

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

        // Set availability
        if (menu.isAvailable()) {
            holder.tvStatus.setText("✅ Tersedia");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.success));
            holder.btnAddToCart.setVisibility(View.VISIBLE);
            holder.cardMenu.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.cardMenu.setAlpha(1.0f);
        } else {
            holder.tvStatus.setText("⚠️ Habis");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.text_gray));
            holder.btnAddToCart.setVisibility(View.GONE);
            holder.cardMenu.setCardBackgroundColor(context.getResources().getColor(R.color.light_gray));
            holder.cardMenu.setAlpha(0.6f);
        }

        // Set favorite icon
        boolean isFavorite = dbHelper.isFavorite(buyerId, menu.getId());
        holder.btnFavorite.setText(isFavorite ? "❤️" : "🤍");

        // Set image (placeholder for now)
        holder.ivMenuImage.setImageResource(R.drawable.ic_book_placeholder);

        // Click listeners
        holder.cardMenu.setOnClickListener(v -> listener.onMenuClick(menu));
        holder.btnAddToCart.setOnClickListener(v -> listener.onAddToCart(menu));
        holder.btnFavorite.setOnClickListener(v -> listener.onToggleFavorite(menu));
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
        TextView btnAddToCart, btnFavorite;

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
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}