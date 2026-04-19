package com.blooddonation.finder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blooddonation.finder.R;
import com.blooddonation.finder.models.BloodRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    public interface OnAcceptListener {
        void onAccept(BloodRequest request);
    }

    private final Context context;
    private final List<BloodRequest> requestList;
    private final OnAcceptListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public RequestAdapter(Context context, List<BloodRequest> requestList, OnAcceptListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        BloodRequest request = requestList.get(position);
        holder.tvBloodGroup.setText("🩸 " + request.getBloodGroup() + " Needed");
        holder.tvRequester.setText("By: " + request.getRequesterName());
        holder.tvHospital.setText("🏥 " + request.getHospitalName());
        holder.tvCity.setText("📍 " + request.getCity());
        holder.tvUrgency.setText("⚡ " + request.getUrgency());
        holder.tvTime.setText("🕐 " + sdf.format(new Date(request.getTimestamp())));

        int urgencyColor;
        switch (request.getUrgency()) {
            case "CRITICAL": urgencyColor = context.getColor(R.color.colorCritical); break;
            case "URGENT":   urgencyColor = context.getColor(R.color.colorUrgent);   break;
            default:         urgencyColor = context.getColor(R.color.colorNormal);    break;
        }
        holder.tvUrgency.setTextColor(urgencyColor);

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(request));
    }

    @Override
    public int getItemCount() { return requestList.size(); }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodGroup, tvRequester, tvHospital, tvCity, tvUrgency, tvTime;
        Button btnAccept;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvRequester  = itemView.findViewById(R.id.tvRequester);
            tvHospital   = itemView.findViewById(R.id.tvHospital);
            tvCity       = itemView.findViewById(R.id.tvCity);
            tvUrgency    = itemView.findViewById(R.id.tvUrgency);
            tvTime       = itemView.findViewById(R.id.tvTime);
            btnAccept    = itemView.findViewById(R.id.btnAccept);
        }
    }
}
