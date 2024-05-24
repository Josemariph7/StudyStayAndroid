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
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.model.Conversation;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversationList;
    private OnItemClickListener listener;
    private ConversationController conversationController;
    private Context context;

    public ConversationAdapter(List<Conversation> conversationList, Context context) {
        this.conversationList = conversationList;
        this.context = context;
        this.conversationController = new ConversationController(context);
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.textViewUser.setText("User " + conversation.getUser2Id());
        if (conversation.getMessages() != null && !conversation.getMessages().isEmpty()) {
            holder.textViewLastMessage.setText(conversation.getMessages().get(conversation.getMessages().size() - 1).getContent());
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(conversation);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(conversation, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUser;
        TextView textViewLastMessage;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
        }
    }

    private void showDeleteDialog(Conversation conversation, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Delete Conversation");
        dialogMessage.setText("Are you sure you want to delete this conversation?");

        AlertDialog alertDialog = builder.create();

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        buttonConfirm.setOnClickListener(v -> {
            conversationController.deleteConversation(conversation.getConversationId(), new ConversationController.ConversationCallback() {
                @Override
                public void onSuccess(Object result) {
                    conversationList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, conversationList.size());
                    alertDialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Log.e("ConversationAdapter", "Error deleting conversation: " + error);
                    alertDialog.dismiss();
                }
            });
        });

        alertDialog.show();
    }
}
