package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * CART ADAPTER - Multi-Stand Support
 * Displays cart items grouped by stand
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.GroupViewHolder> {
    private Context context;
    private List<CartItemGroup> groups;
    private CartListener listener;

    public interface CartListener {
        void onQuantityChanged(CartItem item, int newQty);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(Context context, List<CartItemGroup> groups, CartListener listener) {
        this.context = context;
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_group_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        CartItemGroup group = groups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvStandName, tvSubtotal;
        RecyclerView rvItems;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStandName = itemView.findViewById(R.id.tvStandName);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            rvItems = itemView.findViewById(R.id.rvItems);
        }

        public void bind(CartItemGroup group) {
            tvStandName.setText("🏪 " + group.getStandName());
            tvSubtotal.setText(formatPrice(group.getSubtotal()));

            // Setup nested RecyclerView for items
            CartItemsAdapter itemsAdapter = new CartItemsAdapter(
                    context,
                    group.getItems(),
                    listener
            );
            rvItems.setLayoutManager(new LinearLayoutManager(context));
            rvItems.setAdapter(itemsAdapter);
            rvItems.setNestedScrollingEnabled(false);
        }
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price).replace("IDR", "Rp").replace(",00", "");
    }
}

