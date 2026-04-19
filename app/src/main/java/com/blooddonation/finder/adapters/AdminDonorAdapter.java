package com.blooddonation.finder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.FirebaseDatabase;
import com.blooddonation.finder.R;
import com.blooddonation.finder.models.Donor;
import java.util.List;

public class AdminDonorAdapter extends RecyclerView.Adapter<AdminDonorAdapter.AdminViewHolder> {

    private final Context context;
    private final List<Donor> donorList;

    public AdminDonorAdapter(Context context, List<Donor> donorList) {
        this.context = context;
        this.donorList = donorList;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_donor, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Donor donor = donorList.get(position);
        holder.tvName.setText(donor.getName());
        holder.tvEmail.setText(donor.getEmail());
        holder.tvBloodGroup.setText(donor.getBloodGroup());
        holder.tvCity.setText(donor.getCity());
        holder.tvDonations.setText("Donations: " + donor.getDonationCount());
        holder.switchAvailable.setChecked(donor.isAvailable());

        holder.switchAvailable.setOnCheckedChangeListener((btn, isChecked) -> {
            FirebaseDatabase.getInstance().getReference()
                .child("donors").child(donor.getUid()).child("available").setValue(isChecked);
        });
    }

    @Override
    public int getItemCount() { return donorList.size(); }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvBloodGroup, tvCity, tvDonations;
        Switch switchAvailable;

        AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName         = itemView.findViewById(R.id.tvName);
            tvEmail        = itemView.findViewById(R.id.tvEmail);
            tvBloodGroup   = itemView.findViewById(R.id.tvBloodGroup);
            tvCity         = itemView.findViewById(R.id.tvCity);
            tvDonations    = itemView.findViewById(R.id.tvDonations);
            switchAvailable= itemView.findViewById(R.id.switchAvailable);
        }
    }
}
