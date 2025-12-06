package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    private Context context;
    private List<User> customers;

    public CustomerAdapter(Context context, List<User> customers) {
        this.context = context;
        this.customers = customers;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_item, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        User customer = customers.get(position);

        holder.tvCustomerName.setText(customer.getName());
        holder.tvCustomerEmail.setText("📧 " + customer.getEmail());
        holder.tvCustomerPhone.setText("📱 " + (customer.getPhone() != null ? customer.getPhone() : "-"));

        // Set icon based on type
        if (customer.isMahasiswa()) {
            holder.tvCustomerIcon.setText("🎓");
            holder.tvCustomerType.setText("🎓 Mahasiswa");
            holder.tvCustomerNimNip.setText("NIM: " + (customer.getNimNip() != null ? customer.getNimNip() : "-"));
        } else if (customer.isDosen()) {
            holder.tvCustomerIcon.setText("👨‍🏫");
            holder.tvCustomerType.setText("👨‍🏫 Dosen");
            holder.tvCustomerNimNip.setText("NIP: " + (customer.getNimNip() != null ? customer.getNimNip() : "-"));
        } else {
            holder.tvCustomerIcon.setText("👤");
            holder.tvCustomerType.setText("👤 Customer");
            holder.tvCustomerNimNip.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerIcon, tvCustomerName, tvCustomerType, tvCustomerNimNip,
                tvCustomerEmail, tvCustomerPhone;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerIcon = itemView.findViewById(R.id.tvCustomerIcon);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerType = itemView.findViewById(R.id.tvCustomerType);
            tvCustomerNimNip = itemView.findViewById(R.id.tvCustomerNimNip);
            tvCustomerEmail = itemView.findViewById(R.id.tvCustomerEmail);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
        }
    }
}