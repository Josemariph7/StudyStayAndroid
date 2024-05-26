package com.example.studystayandroid.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversationList;
    private OnItemClickListener listener;
    private ConversationController conversationController;
    private Context context;
    private Long currentUserId;

    public ConversationAdapter(List<Conversation> conversationList, Long currentUserId, Context context) {
        this.conversationList = conversationList;
        this.context = context;
        this.conversationController = new ConversationController(context);
        this.currentUserId = currentUserId;
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

        // Aquí obtenemos el ID del usuario contrario en la conversación
        Long otherUserId = conversation.getUser2Id();
        if (otherUserId.equals(currentUserId)) {
            otherUserId = conversation.getUser1Id();
        }

        // Usamos el UserController para obtener la información del usuario
        UserController userController = new UserController(context);
        Long finalOtherUserId = otherUserId;
        userController.getUserById(otherUserId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onSuccess(User user) {
                holder.textViewUser.setText(user.getName() + " " + user.getLastName());
                byte[] profileImageBytes = user.getProfilePicture();
                if (profileImageBytes != null && profileImageBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                    holder.imageViewProfile.setImageBitmap(bitmap);
                } else {
                    holder.imageViewProfile.setImageResource(R.drawable.defaultprofile);
                }
            }

            @Override
            public void onError(String error) {
                // Manejar el error
                holder.textViewUser.setText("User " + finalOtherUserId);
                holder.imageViewProfile.setImageResource(R.drawable.defaultprofile);
            }
        });

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
        ImageView imageViewProfile;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
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
