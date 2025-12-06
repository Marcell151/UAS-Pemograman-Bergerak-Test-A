package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapterCustomer extends RecyclerView.Adapter<OrderAdapterCustomer.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapterCustomer(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item_customer, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        holder.tvOrderId.setText("#" + order.getId());
        holder.tvOrderDate.setText(order.getCreatedAt());
        holder.tvStandName.setText("🏪 " + order.getStandName());
        holder.tvOrderTotal.setText(formatter.format(order.getTotal()));
        holder.tvOrderEmoji.setText(order.getStatusEmoji());
        holder.tvOrderStatus.setText(order.getStatusEmoji() + " " + order.getStatusText());

        // Set status color
        int statusColor;
        switch (order.getStatus()) {
            case "pending":
                statusColor = context.getResources().getColor(R.color.warning);
                break;
            case "processing":
                statusColor = context.getResources().getColor(R.color.info);
                break;
            case "ready":
                statusColor = context.getResources().getColor(R.color.success);
                break;
            case "completed":
                statusColor = context.getResources().getColor(R.color.secondary);
                break;
            case "cancelled":
                statusColor = context.getResources().getColor(R.color.danger);
                break;
            default:
                statusColor = context.getResources().getColor(R.color.text_gray);
        }
        holder.cardStatus.setCardBackgroundColor(statusColor);

        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvStandName, tvOrderTotal, tvOrderStatus, tvOrderEmoji;
        CardView cardStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvStandName = itemView.findViewById(R.id.tvStandName);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderEmoji = itemView.findViewById(R.id.tvOrderEmoji);
            cardStatus = itemView.findViewById(R.id.cardStatus);
        }
    }
}