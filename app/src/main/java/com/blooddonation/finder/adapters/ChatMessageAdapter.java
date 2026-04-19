package com.blooddonation.finder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blooddonation.finder.R;
import com.blooddonation.finder.models.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private final List<ChatMessage> messages;
    private final String currentUserId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public ChatMessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String timeStr = timeFormat.format(new Date(msg.getTimestamp()));

        boolean isMine = currentUserId.equals(msg.getSenderId());
        if (isMine) {
            holder.layoutMine.setVisibility(View.VISIBLE);
            holder.layoutTheirs.setVisibility(View.GONE);
            holder.tvMineText.setText(msg.getText());
            holder.tvMineTime.setText(timeStr);
        } else {
            holder.layoutMine.setVisibility(View.GONE);
            holder.layoutTheirs.setVisibility(View.VISIBLE);
            holder.tvTheirName.setText(msg.getSenderName());
            holder.tvTheirsText.setText(msg.getText());
            holder.tvTheirsTime.setText(timeStr);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMine, layoutTheirs;
        TextView tvMineText, tvMineTime;
        TextView tvTheirName, tvTheirsText, tvTheirsTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMine   = itemView.findViewById(R.id.layoutMine);
            layoutTheirs = itemView.findViewById(R.id.layoutTheirs);
            tvMineText   = itemView.findViewById(R.id.tvMineText);
            tvMineTime   = itemView.findViewById(R.id.tvMineTime);
            tvTheirName  = itemView.findViewById(R.id.tvTheirName);
            tvTheirsText = itemView.findViewById(R.id.tvTheirsText);
            tvTheirsTime = itemView.findViewById(R.id.tvTheirsTime);
        }
    }
}
