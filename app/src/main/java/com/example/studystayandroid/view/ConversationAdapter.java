package com.example.studystayandroid.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
                    Glide.with(context)
                            .asBitmap()
                            .load(profileImageBytes)
                            .transform(new CircleCrop())
                            .into(holder.imageViewProfile);
                } else {
                    Glide.with(context)
                            .load(R.drawable.defaultprofile)
                            .transform(new CircleCrop())
                            .into(holder.imageViewProfile);
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
            if (longClickListener != null) {
                longClickListener.onItemLongClick(conversation);
            }
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

    public interface OnItemLongClickListener {
        void onItemLongClick(Conversation conversation);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        this.longClickListener = (OnItemLongClickListener) listener;
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

    private void showOptionsDialog(Conversation conversation, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_options, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Options");

        Button buttonDelete = dialogView.findViewById(R.id.buttonOption1);
        buttonDelete.setText("Delete Conversation");
        Button buttonViewProfile = dialogView.findViewById(R.id.buttonOption2);
        buttonViewProfile.setText("View User Profile");
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        buttonDelete.setOnClickListener(v -> {
            showDeleteDialog(conversation, position);
            dialog.dismiss();
        });

        buttonViewProfile.setOnClickListener(v -> {
            openUserProfile(conversation);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openUserProfile(Conversation conversation) {
        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id() : conversation.getUser1Id();
        UserController userController = new UserController(context);
        userController.getUserById(otherUserId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {
                User user = (User) result;
                StrangeProfileFragment profileFragment = StrangeProfileFragment.newInstance(user);
                // Assuming you have a method in the activity to handle fragment transactions
                ((DashboardActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onSuccess(User user) {
                StrangeProfileFragment profileFragment = StrangeProfileFragment.newInstance(user);
                ((DashboardActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onError(String error) {
                Log.e("ConversationAdapter", "Error fetching user profile: " + error);
            }
        });
    }


    private void showDeleteDialog(Conversation conversation, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Delete Conversation");
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        dialogMessage.setText("Are you sure you want to delete this conversation?");
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            conversationController.deleteConversation(conversation.getConversationId(), new ConversationController.ConversationCallback() {
                @Override
                public void onSuccess(Conversation createdConversation) {
                    conversationList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, conversationList.size());
                    dialog.dismiss();
                }

                @Override
                public void onSuccess(Object result) {
                    conversationList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, conversationList.size());
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Log.e("ConversationAdapter", "Error deleting conversation: " + error);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

}
