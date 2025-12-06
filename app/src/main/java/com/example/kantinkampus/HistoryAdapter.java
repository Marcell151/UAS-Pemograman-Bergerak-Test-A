package com.example.kantinkampus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kantinkampus.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private Context context;
    private List<History> historyList;

    public HistoryAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History history = historyList.get(position);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        holder.tvHistoryItems.setText(history.getItems());
        holder.tvHistoryTotal.setText(formatter.format(history.getTotal()));
        holder.tvHistoryDate.setText(history.getTanggal());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryItems, tvHistoryTotal, tvHistoryDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryItems = itemView.findViewById(R.id.tvHistoryItems);
            tvHistoryTotal = itemView.findViewById(R.id.tvHistoryTotal);
            tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
        }
    }
}