package com.example.studystayandroid.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messageList;
    private Long currentUserId; // ID del usuario actual

    public MessageAdapter(List<Message> messageList, Long currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewDateTime;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
        }

        void bind(Message message) {
            textViewContent.setText(message.getContent());
            textViewDateTime.setText(message.getDateTime().toString());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewDateTime;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
        }

        void bind(Message message) {
            textViewContent.setText(message.getContent());
            textViewDateTime.setText(message.getDateTime().toString());
        }
    }
}
