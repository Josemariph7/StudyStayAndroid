package com.example.studystayandroid.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studystayandroid.R;
import com.example.studystayandroid.model.ForumTopic;
import com.example.studystayandroid.model.User;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ForumTopicAdapter extends RecyclerView.Adapter<ForumTopicAdapter.ViewHolder> {

    private List<ForumTopic> topics;
    private OnTopicClickListener listener;
    private User currentUser;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public ForumTopicAdapter(List<ForumTopic> topics, User currentUser, OnTopicClickListener listener) {
        this.topics = topics;
        this.listener = listener;
        this.currentUser = currentUser;
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
        holder.titleTextView.setText(topic.getTitle());
        holder.descriptionTextView.setText(topic.getDescription());
        holder.authorTextView.setText(topic.getAuthor().getName() + " " + topic.getAuthor().getLastName());
        holder.creationDateTextView.setText(topic.getDateTime().format(DATE_FORMATTER));
        holder.itemView.setOnClickListener(v -> listener.onTopicClick(topic));

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(holder.itemView.getContext(), topic);
            return true;
        });
    }

    private void showDeleteDialog(Context context, ForumTopic topic) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (topic.getAuthor().getUserId().equals(currentUser.getUserId())) {
            builder.setTitle("Delete Topic")
                    .setMessage("Are you sure you want to delete this topic?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Call delete method from controller
                        // forumTopicController.deleteTopic(topic.getTopicId(), callback)
                    })
                    .setNegativeButton("No", null);
        } else {
            builder.setTitle("Cannot Delete Topic")
                    .setMessage("You cannot delete this topic because you did not create it.")
                    .setPositiveButton("OK", null);
        }
        builder.show();
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
