package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * MENU ADAPTER FOR BUYER
 * Displays menus in grid with add to cart and favorite options
 */
public class MenuAdapterBuyer extends RecyclerView.Adapter<MenuAdapterBuyer.ViewHolder> {
    private Context context;
    private List<Menu> menus;
    private MenuListener listener;

    // ✅ ADD THIS INTERFACE HERE:
    public interface MenuListener {
        void onMenuClick(Menu menu);
        void onAddToCart(Menu menu);
        void onFavoriteClick(Menu menu);
    }

    public MenuAdapterBuyer(Context context, List<Menu> menus, MenuListener listener) {
        this.context = context;
        this.menus = menus;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Menu menu = menus.get(position);
        holder.bind(menu);
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardMenu;
        TextView tvMenuName, tvPrice, tvCategory, tvRating, tvStatus;
        ImageButton btnFavorite, btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMenu = itemView.findViewById(R.id.cardMenu);
            tvMenuName = itemView.findViewById(R.id.tvMenuName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        public void bind(Menu menu) {
            tvMenuName.setText(menu.getNama());
            tvPrice.setText(formatPrice(menu.getHarga()));
            tvCategory.setText(menu.getKategori());

            // Rating
            if (menu.getTotalReviews() > 0) {
                tvRating.setVisibility(View.VISIBLE);
                tvRating.setText(String.format("⭐ %.1f (%d)", menu.getAverageRating(), menu.getTotalReviews()));
            } else {
                tvRating.setVisibility(View.GONE);
            }

            // Status
            if (menu.getStatus().equals("available")) {
                tvStatus.setVisibility(View.GONE);
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
            } else {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("Habis");
                btnAddToCart.setEnabled(false);
                btnAddToCart.setAlpha(0.5f);
            }

            // Click listeners
            cardMenu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(menu);
                }
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(menu);
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCart(menu);
                }
            });
        }
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price).replace("IDR", "Rp").replace(",00", "");
    }
}