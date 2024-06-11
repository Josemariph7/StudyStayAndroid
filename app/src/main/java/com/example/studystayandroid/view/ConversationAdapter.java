/*
 * StudyStay © 2024
 *
 * All rights reserved.
 *
 * This software and associated documentation files (the "Software") are owned by StudyStay. Unauthorized copying, distribution, or modification of this Software is strictly prohibited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this Software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * StudyStay
 * José María Pozo Hidalgo
 * Email: josemariph7@gmail.com
 *
 *
 */

package com.example.studystayandroid.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Adaptador para mostrar una lista de conversaciones en un RecyclerView.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversationList;
    private OnItemClickListener listener;
    private ConversationController conversationController;
    private Context context;
    private Long currentUserId;

    /**
     * Constructor del adaptador de conversaciones.
     *
     * @param conversationList Lista de conversaciones a mostrar.
     * @param currentUserId    ID del usuario actual.
     * @param context          Contexto para acceder a los recursos.
     */
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

        // Obtener el ID del otro usuario en la conversación
        Long otherUserId = conversation.getUser2Id();
        if (otherUserId.equals(currentUserId)) {
            otherUserId = conversation.getUser1Id();
        }

        // Usar UserController para obtener la información del usuario
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

    /**
     * Configura el listener de clic en un item.
     *
     * @param listener Listener a configurar.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Interfaz para el listener de clic en un item.
     */
    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    /**
     * Interfaz para el listener de clic largo en un item.
     */
    public interface OnItemLongClickListener {
        void onItemLongClick(Conversation conversation);
    }

    private OnItemLongClickListener longClickListener;

    /**
     * Configura el listener de clic largo en un item.
     *
     * @param listener Listener a configurar.
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    /**
     * ViewHolder para los items de conversación.
     */
    class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUser;
        TextView textViewLastMessage;
        ImageView imageViewProfile;

        /**
         * Constructor del ViewHolder para las conversaciones.
         *
         * @param itemView La vista del item.
         */
        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
        }
    }

    /**
     * Muestra el diálogo de opciones para la conversación seleccionada.
     *
     * @param conversation La conversación seleccionada.
     * @param position     La posición de la conversación en la lista.
     */
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

    /**
     * Abre el perfil del usuario de la conversación seleccionada.
     *
     * @param conversation La conversación seleccionada.
     */
    private void openUserProfile(Conversation conversation) {
        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id() : conversation.getUser1Id();
        UserController userController = new UserController(context);
        userController.getUserById(otherUserId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {
                User user = (User) result;
                StrangeProfileFragment profileFragment = StrangeProfileFragment.newInstance(user);
                // Asumimos que tienes un método en la actividad para manejar transacciones de fragmentos
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

    /**
     * Muestra el diálogo de confirmación para eliminar una conversación.
     *
     * @param conversation La conversación a eliminar.
     * @param position     La posición de la conversación en la lista.
     */
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
