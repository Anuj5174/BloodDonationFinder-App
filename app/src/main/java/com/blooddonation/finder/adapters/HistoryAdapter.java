package com.blooddonation.finder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blooddonation.finder.R;
import com.blooddonation.finder.models.DonationHistory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<DonationHistory> historyList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public HistoryAdapter(Context context, List<DonationHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        DonationHistory history = historyList.get(position);
        holder.tvRecipient.setText("Donated to: " + history.getRecipientName());
        holder.tvBloodGroup.setText("🩸 " + history.getBloodGroup());
        holder.tvLocation.setText("📍 " + history.getLocation());
        holder.tvDate.setText("📅 " + sdf.format(new Date(history.getDonationDate())));
        holder.tvIndex.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() { return historyList.size(); }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipient, tvBloodGroup, tvLocation, tvDate, tvIndex;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipient = itemView.findViewById(R.id.tvRecipient);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvIndex = itemView.findViewById(R.id.tvIndex);
        }
    }
}
