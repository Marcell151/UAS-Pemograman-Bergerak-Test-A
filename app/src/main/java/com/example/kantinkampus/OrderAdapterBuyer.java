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

/**
 * ORDER ADAPTER FOR BUYER
 * Displays order history with status indicators
 */
public class OrderAdapterBuyer extends RecyclerView.Adapter<OrderAdapterBuyer.ViewHolder> {
    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapterBuyer(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item_buyer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardOrder;
        TextView tvOrderId, tvStandName, tvTotal, tvStatus, tvDate, tvPaymentMethod;
        View statusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.cardOrder);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStandName = itemView.findViewById(R.id.tvStandName);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(Order order) {
            tvOrderId.setText("Pesanan #" + order.getId());
            tvStandName.setText("🏪 " + order.getStandName());
            tvTotal.setText(formatPrice(order.getTotal()));
            tvDate.setText("📅 " + order.getCreatedAt());
            tvPaymentMethod.setText("💳 " + order.getPaymentMethod());

            // Set status with color
            String status = order.getStatus();
            String statusText = "";
            int statusColor = context.getResources().getColor(R.color.text_gray);

            switch (status) {
                case "pending_payment":
                    statusText = "⏳ Menunggu Pembayaran";
                    statusColor = context.getResources().getColor(R.color.warning);
                    break;
                case "pending_verification":
                    statusText = "🔍 Menunggu Verifikasi";
                    statusColor = context.getResources().getColor(R.color.warning);
                    break;
                case "verified":
                    statusText = "✅ Terverifikasi";
                    statusColor = context.getResources().getColor(R.color.info);
                    break;
                case "cooking":
                    statusText = "👨‍🍳 Sedang Dimasak";
                    statusColor = context.getResources().getColor(R.color.info);
                    break;
                case "ready":
                    statusText = "✅ Siap Diambil!";
                    statusColor = context.getResources().getColor(R.color.success);
                    break;
                case "completed":
                    statusText = "🎉 Selesai";
                    statusColor = context.getResources().getColor(R.color.success);
                    break;
                case "cancelled":
                    statusText = "❌ Dibatalkan";
                    statusColor = context.getResources().getColor(R.color.danger);
                    break;
            }

            tvStatus.setText(statusText);
            tvStatus.setTextColor(statusColor);
            statusIndicator.setBackgroundColor(statusColor);

            // Click listener
            cardOrder.setOnClickListener(v -> listener.onOrderClick(order));
        }
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price).replace("IDR", "Rp").replace(",00", "");
    }
}