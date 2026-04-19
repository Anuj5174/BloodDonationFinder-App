package com.blooddonation.finder.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.blooddonation.finder.R;
import com.blooddonation.finder.activities.DonorProfileActivity;
import com.blooddonation.finder.models.Donor;
import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    private final Context context;
    private final List<Donor> donorList;

    public DonorAdapter(Context context, List<Donor> donorList) {
        this.context = context;
        this.donorList = donorList;
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donor, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        Donor donor = donorList.get(position);

        holder.tvName.setText(donor.getName());
        holder.tvBloodGroup.setText(donor.getBloodGroup());
        holder.tvCity.setText("📍 " + donor.getCity());
        holder.tvDonations.setText("🩸 " + donor.getDonationCount() + " donations");
        holder.tvAvailability.setText(donor.isAvailable() ? "🟢 Available" : "🔴 Not Available");
        holder.tvAvailability.setTextColor(context.getColor(
            donor.isAvailable() ? R.color.colorAvailable : R.color.colorNotAvailable));

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DonorProfileActivity.class);
            intent.putExtra("donorId", donor.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return donorList.size();
    }

    static class DonorViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvName, tvBloodGroup, tvCity, tvDonations, tvAvailability;

        DonorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvName = itemView.findViewById(R.id.tvDonorName);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvDonations = itemView.findViewById(R.id.tvDonations);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
        }
    }
}
