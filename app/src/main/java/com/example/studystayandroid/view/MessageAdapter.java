package com.example.studystayandroid.view;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.MessageController;
import com.example.studystayandroid.model.Message;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messageList;
    private Long currentUserId;
    private Context context;
    private MessageController messageController;

    public MessageAdapter(List<Message> messageList, Long currentUserId, Context context) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.context = context;
        this.messageController = new MessageController(context);
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
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (message.getSenderId().equals(currentUserId)) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Delete Message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Delete the message
                            deleteMessage(message);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private void showDeleteDialog(Message message, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Delete Message");
        dialogMessage.setText("Are you sure you want to delete this message?");

        AlertDialog alertDialog = builder.create();

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        buttonConfirm.setOnClickListener(v -> {
            messageController.deleteMessage(message.getMessageId(), new MessageController.MessageCallback() {
                @Override
                public void onSuccess(Object result) {
                    messageList.remove(position);
                    notifyItemRemoved(position);
                    alertDialog.dismiss();
                }
                @Override
                public void onError(String error) {
                    Log.e("MessageAdapter", "Error deleting message: " + error);
                    alertDialog.dismiss();
                }
            });
        });
        alertDialog.show();
    }

    private void deleteMessage(Message message) {
        MessageController messageController = new MessageController(context);
        messageController.deleteMessage(message.getMessageId(), new MessageController.MessageCallback() {
            @Override
            public void onSuccess(Object result) {
                messageList.remove(message);
                notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("MessageAdapter", "Error deleting message: " + error);
            }
        });
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewDateTime;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);

            itemView.setOnLongClickListener(v -> {
                showDeleteDialog(messageList.get(getAdapterPosition()), getAdapterPosition());
                return true;
            });
        }

        void bind(Message message) {
            textViewContent.setText(message.getContent());
            textViewDateTime.setText(message.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        TextView textViewDateTime;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);

            itemView.setOnLongClickListener(v -> {
                showDeleteDialog(messageList.get(getAdapterPosition()), getAdapterPosition());
                return true;
            });
        }

        void bind(Message message) {
            textViewContent.setText(message.getContent());
            textViewDateTime.setText(message.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }
}
