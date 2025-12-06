package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemListener listener;
    private DBHelper dbHelper;

    public interface OnCartItemListener {
        void onQuantityChanged();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
        this.dbHelper = new DBHelper(context);
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        Menu menu = item.getMenu();

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        holder.tvCartItemName.setText(menu.getNama());
        holder.tvCartItemPrice.setText(formatter.format(menu.getHarga()));
        holder.tvCartItemQty.setText(String.valueOf(item.getQty()));
        holder.tvCartItemSubtotal.setText(formatter.format(item.getSubtotal()));

        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQty = item.getQty() + 1;
                dbHelper.updateCartQty(item.getId(), newQty);
                listener.onQuantityChanged();
            }
        });

        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQty = item.getQty() - 1;
                if (newQty <= 0) {
                    // Show confirmation before removing
                    new androidx.appcompat.app.AlertDialog.Builder(context)
                            .setTitle("Hapus Item")
                            .setMessage("Hapus " + menu.getNama() + " dari keranjang?")
                            .setPositiveButton("Ya", (dialog, which) -> {
                                dbHelper.updateCartQty(item.getId(), 0);
                                listener.onQuantityChanged();
                            })
                            .setNegativeButton("Tidak", null)
                            .show();
                } else {
                    dbHelper.updateCartQty(item.getId(), newQty);
                    listener.onQuantityChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvCartItemName, tvCartItemPrice, tvCartItemQty, tvCartItemSubtotal;
        TextView btnPlus, btnMinus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCartItemName = itemView.findViewById(R.id.tvCartItemName);
            tvCartItemPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvCartItemQty = itemView.findViewById(R.id.tvCartItemQty);
            tvCartItemSubtotal = itemView.findViewById(R.id.tvCartItemSubtotal);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}