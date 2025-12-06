package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * ORDER ADAPTER FOR SELLER
 * Shows order list with actions based on status
 */
public class OrderAdapterSeller extends RecyclerView.Adapter<OrderAdapterSeller.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onVerifyPayment(Order order);
        void onUpdateStatus(Order order);
        void onCancelOrder(Order order);
    }

    public OrderAdapterSeller(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item_seller, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Set order info
        holder.tvOrderId.setText("Order #" + order.getId());
        holder.tvBuyerName.setText("👤 " + order.getUserName());
        holder.tvOrderTotal.setText(order.getFormattedTotal());
        holder.tvOrderDate.setText("📅 " + order.getCreatedAt());
        holder.tvPaymentMethod.setText("💳 " + order.getPaymentMethod());

        // Set status with color
        String status = order.getStatus();
        holder.tvOrderStatus.setText(order.getStatusDisplay());

        int statusColor;
        int bgColor;

        switch (status) {
            case "pending_verification":
                statusColor = context.getResources().getColor(R.color.warning);
                bgColor = context.getResources().getColor(R.color.white);
                break;
            case "verified":
                statusColor = context.getResources().getColor(R.color.info);
                bgColor = context.getResources().getColor(R.color.white);
                break;
            case "cooking":
                statusColor = context.getResources().getColor(R.color.primary);
                bgColor = context.getResources().getColor(R.color.light_orange);
                break;
            case "ready":
                statusColor = context.getResources().getColor(R.color.success);
                bgColor = context.getResources().getColor(R.color.white);
                break;
            case "completed":
                statusColor = context.getResources().getColor(R.color.text_gray);
                bgColor = context.getResources().getColor(R.color.light_gray);
                break;
            case "cancelled":
                statusColor = context.getResources().getColor(R.color.danger);
                bgColor = context.getResources().getColor(R.color.light_gray);
                break;
            default:
                statusColor = context.getResources().getColor(R.color.text_dark);
                bgColor = context.getResources().getColor(R.color.white);
        }

        holder.tvOrderStatus.setTextColor(statusColor);
        holder.cardOrder.setCardBackgroundColor(bgColor);

        // Show/hide action buttons based on status
        holder.layoutActions.setVisibility(View.VISIBLE);
        holder.btnVerify.setVisibility(View.GONE);
        holder.btnUpdateStatus.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);

        if ("pending_verification".equals(status)) {
            holder.btnVerify.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else if ("verified".equals(status) || "cooking".equals(status) || "ready".equals(status)) {
            holder.btnUpdateStatus.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else if ("completed".equals(status) || "cancelled".equals(status)) {
            holder.layoutActions.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.cardOrder.setOnClickListener(v -> listener.onOrderClick(order));
        holder.btnVerify.setOnClickListener(v -> listener.onVerifyPayment(order));
        holder.btnUpdateStatus.setOnClickListener(v -> listener.onUpdateStatus(order));
        holder.btnCancel.setOnClickListener(v -> listener.onCancelOrder(order));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cardOrder;
        TextView tvOrderId, tvBuyerName, tvOrderTotal, tvOrderDate;
        TextView tvOrderStatus, tvPaymentMethod;
        View layoutActions;
        TextView btnVerify, btnUpdateStatus, btnCancel;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            cardOrder = itemView.findViewById(R.id.cardOrder);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvBuyerName = itemView.findViewById(R.id.tvBuyerName);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnVerify = itemView.findViewById(R.id.btnVerify);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}