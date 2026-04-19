package com.blooddonation.finder.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blooddonation.finder.databinding.ActivityChatBinding;
import com.blooddonation.finder.models.ChatMessage;
import com.blooddonation.finder.adapters.ChatMessageAdapter;
import com.blooddonation.finder.utils.LocaleHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private String currentUserName;
    private String otherUserId;
    private String otherUserName;
    private String chatId;
    
    private List<ChatMessage> messageList = new ArrayList<>();
    private ChatMessageAdapter adapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chat with " + otherUserName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Chat ID is the combination of both UIDs sorted alphabetically to ensure same ID for both
        if (currentUserId.compareTo(otherUserId) < 0) {
            chatId = currentUserId + "_" + otherUserId;
        } else {
            chatId = otherUserId + "_" + currentUserId;
        }

        setupRecyclerView();
        loadCurrentUserName();
        listenForMessages();

        binding.fabSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(layoutManager);
        binding.recyclerMessages.setAdapter(adapter);
    }

    private void loadCurrentUserName() {
        mDatabase.child("donors").child(currentUserId).child("name")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUserName = snapshot.getValue(String.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    private void listenForMessages() {
        mDatabase.child("chats").child(chatId).child("messages")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    messageList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ChatMessage msg = ds.getValue(ChatMessage.class);
                        if (msg != null) messageList.add(msg);
                    }
                    adapter.notifyDataSetChanged();
                    if (messageList.size() > 0) {
                        binding.recyclerMessages.smoothScrollToPosition(messageList.size() - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        ChatMessage msg = new ChatMessage(currentUserId, currentUserName, text);
        String msgId = mDatabase.child("chats").child(chatId).child("messages").push().getKey();
        msg.setMessageId(msgId);

        mDatabase.child("chats").child(chatId).child("messages").child(msgId).setValue(msg)
            .addOnSuccessListener(unused -> binding.etMessage.setText(""))
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
