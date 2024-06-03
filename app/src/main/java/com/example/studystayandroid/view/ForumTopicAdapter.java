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

public class ForumTopicAdapter extends RecyclerView.Adapter<ForumTopicAdapter.ViewHolder> {

    private List<ForumTopic> topics;
    private OnTopicClickListener listener;
    private User currentUser;
    private ForumFragment forumFragment;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

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

    private void showOptionsDialog(Context context, ForumTopic topic) {
        if (topic.getAuthor().getUserId().equals(currentUser.getUserId())) {
            showDeleteTopicDialog(context, topic);
        } else {
            showViewProfileDialog(context, topic.getAuthor());
        }
    }

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

    public interface OnTopicClickListener {
        void onTopicClick(ForumTopic topic);
    }
}
