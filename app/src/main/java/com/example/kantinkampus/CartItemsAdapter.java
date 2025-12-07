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
 * CART ITEMS ADAPTER
 * Displays individual cart items within a stand group
 */
class CartItemsAdapter extends RecyclerView.Adapter<CartItemsAdapter.ItemViewHolder> {
    private Context context;
    private List<CartItem> items;
    private CartAdapter.CartListener listener;

    public CartItemsAdapter(Context context, List<CartItem> items, CartAdapter.CartListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvMenuName, tvPrice, tvQuantity, tvSubtotal, tvNotes;
        ImageButton btnMinus, btnPlus, btnDelete;
        CardView cardItem;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMenuName = itemView.findViewById(R.id.tvMenuName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cardItem = itemView.findViewById(R.id.cardItem);
        }

        public void bind(CartItem item) {
            Menu menu = item.getMenu();

            tvMenuName.setText(menu.getNama());
            tvPrice.setText(formatPrice(menu.getHarga()) + " × ");
            tvQuantity.setText(String.valueOf(item.getQty()));
            tvSubtotal.setText(formatPrice(item.getSubtotal()));

            // Show notes if exists
            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                tvNotes.setVisibility(View.VISIBLE);
                tvNotes.setText("📝 " + item.getNotes());
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            // Minus button
            btnMinus.setOnClickListener(v -> {
                int newQty = item.getQty() - 1;
                if (newQty <= 0) {
                    listener.onItemRemoved(item);
                } else {
                    listener.onQuantityChanged(item, newQty);
                }
            });

            // Plus button
            btnPlus.setOnClickListener(v -> {
                int newQty = item.getQty() + 1;
                listener.onQuantityChanged(item, newQty);
            });

            // Delete button
            btnDelete.setOnClickListener(v -> listener.onItemRemoved(item));
        }
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price).replace("IDR", "Rp").replace(",00", "");
    }
}
