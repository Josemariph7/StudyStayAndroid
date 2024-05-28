package com.example.studystayandroid.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ForumCommentController;
import com.example.studystayandroid.model.ForumComment;
import com.example.studystayandroid.model.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<ForumComment> comments;
    private Context context;
    private User currentUser;
    private DiscussionFragment discussionFragment;

    public CommentAdapter(Context context, List<ForumComment> comments, User currentUser, DiscussionFragment discussionFragment) {
        this.context = context;
        this.comments = comments;
        this.currentUser = currentUser;
        this.discussionFragment = discussionFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumComment comment = comments.get(position);
        holder.authorTextView.setText(comment.getAuthor().getName() + " " + comment.getAuthor().getLastName());
        holder.contentTextView.setText(comment.getContent());
        holder.dateTextView.setText(comment.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        User user = comment.getAuthor();
        Log.d("CommentAdapter", "User: " + user.toString());
        byte[] profileImageBytes = user.getProfilePicture();

        if (profileImageBytes != null && profileImageBytes.length > 0) {
            Log.d("CommentAdapter", "Loading profile image for user: " + user.getName());
            Glide.with(context)
                    .asBitmap()
                    .load(profileImageBytes)
                    .transform(new CircleCrop())
                    .into(holder.imageViewProfile);
        } else {
            Log.d("CommentAdapter", "Loading default profile image for user: " + user.getName());
            Glide.with(context)
                    .load(R.drawable.defaultprofile)
                    .transform(new CircleCrop())
                    .into(holder.imageViewProfile);
        }

        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(comment);
            return true;
        });
    }

    private void showOptionsDialog(ForumComment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (comment.getAuthor().getUserId().equals(currentUser.getUserId())) {
            builder.setTitle("Delete Comment")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        ForumCommentController commentController = new ForumCommentController(context);
                        commentController.deleteComment(comment.getCommentId(), new ForumCommentController.CommentCallback() {
                            @Override
                            public void onSuccess(ForumComment comment) {
                                discussionFragment.loadComments(); // Recargar los comentarios
                            }

                            @Override
                            public void onSuccess(Object result) {
                                discussionFragment.loadComments(); // Recargar los comentarios
                            }

                            @Override
                            public void onError(String error) {
                                Log.e("CommentAdapter", "Error deleting comment: " + error);
                            }
                        });

                    })
                    .setNegativeButton("No", null);
        } else {
            builder.setTitle("Comment Options")
                    .setItems(new String[]{"View User Profile"}, (dialog, which) -> {
                        if (which == 0) {
                            discussionFragment.openStrangeProfile(comment.getAuthor());
                        }
                    });
        }
        builder.show();
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView authorTextView;
        public TextView contentTextView;
        public TextView dateTextView;
        public ImageView imageViewProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tvCommentAuthor);
            contentTextView = itemView.findViewById(R.id.tvCommentContent);
            dateTextView = itemView.findViewById(R.id.tvCommentDateTime);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
        }
    }
}