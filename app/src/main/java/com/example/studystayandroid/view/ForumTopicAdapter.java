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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ForumTopicController;
import com.example.studystayandroid.model.ForumTopic;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adaptador para mostrar una lista de temas del foro en un RecyclerView.
 */
public class ForumTopicAdapter extends RecyclerView.Adapter<ForumTopicAdapter.ViewHolder> {

    private List<ForumTopic> topics;
    private OnTopicClickListener listener;
    private User currentUser;
    private ForumFragment forumFragment;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    /**
     * Constructor para el adaptador de temas del foro.
     *
     * @param topics Lista de temas del foro.
     * @param currentUser Usuario actual.
     * @param listener Listener para manejar clics en los temas.
     * @param forumFragment Fragmento del foro.
     */
    public ForumTopicAdapter(List<ForumTopic> topics, User currentUser, OnTopicClickListener listener, ForumFragment forumFragment) {
        this.topics = topics;
        this.listener = listener;
        this.currentUser = currentUser;
        this.forumFragment = forumFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forum_topic, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumTopic topic = topics.get(position);

        if (topic != null) {
            holder.titleTextView.setText(topic.getTitle());
            holder.descriptionTextView.setText(topic.getDescription());
            holder.authorTextView.setText(topic.getAuthor().getName() + " " + topic.getAuthor().getLastName());
            holder.creationDateTextView.setText(topic.getDateTime().format(DATE_FORMATTER));
            holder.itemView.setOnClickListener(v -> listener.onTopicClick(topic));

            holder.itemView.setOnLongClickListener(v -> {
                showOptionsDialog(holder.itemView.getContext(), topic);
                return true;
            });
        } else {
            Log.e("ForumTopicAdapter", "ForumTopic is null at position: " + position);
        }
    }

    /**
     * Muestra el cuadro de diálogo de opciones para un tema del foro.
     *
     * @param context Contexto de la aplicación.
     * @param topic Tema del foro.
     */
    private void showOptionsDialog(Context context, ForumTopic topic) {
        if (topic.getAuthor().getUserId().equals(currentUser.getUserId())) {
            showDeleteTopicDialog(context, topic);
        } else {
            showViewProfileDialog(context, topic.getAuthor());
        }
    }

    /**
     * Muestra el cuadro de diálogo de confirmación para eliminar un tema.
     *
     * @param context Contexto de la aplicación.
     * @param topic Tema del foro.
     */
    private void showDeleteTopicDialog(Context context, ForumTopic topic) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Delete Topic");
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        dialogMessage.setText("Are you sure you want to delete this topic?");
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            ForumTopicController topicController = new ForumTopicController(context);
            topicController.deleteTopic(topic.getTopicId(), new ForumTopicController.TopicCallback() {
                @Override
                public void onSuccess(ForumTopic topic) {
                    forumFragment.reloadTopics();
                    dialog.dismiss();
                }

                @Override
                public void onSuccess(Object result) {
                    forumFragment.reloadTopics();
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Log.e("ForumTopicAdapter", "Error deleting topic: " + error);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    /**
     * Muestra el cuadro de diálogo para ver el perfil del autor del tema.
     *
     * @param context Contexto de la aplicación.
     * @param author Autor del tema del foro.
     */
    private void showViewProfileDialog(Context context, User author) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_view_profile, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Topic Options");
        Button buttonViewProfile = dialogView.findViewById(R.id.buttonViewProfile);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        buttonViewProfile.setOnClickListener(v -> {
            forumFragment.openStrangeProfile(author);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    /**
     * ViewHolder para los temas del foro.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView authorTextView;
        public TextView creationDateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvTopicTitle);
            descriptionTextView = itemView.findViewById(R.id.tvTopicDescription);
            authorTextView = itemView.findViewById(R.id.tvTopicAuthor);
            creationDateTextView = itemView.findViewById(R.id.tvTopicCreationDate);
        }
    }

    /**
     * Interfaz para manejar los clics en los temas del foro.
     */
    public interface OnTopicClickListener {
        void onTopicClick(ForumTopic topic);
    }
}
