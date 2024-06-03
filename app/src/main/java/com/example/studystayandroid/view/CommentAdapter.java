package com.example.studystayandroid.view;

import android.annotation.SuppressLint;
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
import com.example.studystayandroid.controller.ForumCommentController;
import com.example.studystayandroid.model.ForumComment;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
        if (comment.getAuthor().getUserId().equals(currentUser.getUserId())) {
            showDeleteCommentDialog(comment);
        } else {
            showViewProfileDialog(comment.getAuthor());
        }
    }

    private void showDeleteCommentDialog(ForumComment comment) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Delete Comment");
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        dialogMessage.setText("Are you sure you want to delete this comment?");
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            ForumCommentController commentController = new ForumCommentController(context);
            commentController.deleteComment(comment.getCommentId(), new ForumCommentController.CommentCallback() {
                @Override
                public void onSuccess(ForumComment comment) {
                    discussionFragment.loadComments();
                    dialog.dismiss();
                }

                @Override
                public void onSuccess(Object result) {
                    discussionFragment.loadComments();
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Log.e("CommentAdapter", "Error deleting comment: " + error);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void showViewProfileDialog(User author) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_view_profile, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Comment Options");
        Button buttonViewProfile = dialogView.findViewById(R.id.buttonViewProfile);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        buttonViewProfile.setOnClickListener(v -> {
            discussionFragment.openStrangeProfile(author);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
