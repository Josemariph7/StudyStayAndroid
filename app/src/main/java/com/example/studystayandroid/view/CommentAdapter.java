package com.example.studystayandroid.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studystayandroid.R;
import com.example.studystayandroid.model.ForumComment;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<ForumComment> comments;

    public CommentAdapter(List<ForumComment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumComment comment = comments.get(position);
        holder.authorTextView.setText(comment.getAuthor().getName());
        holder.contentTextView.setText(comment.getContent());
        holder.dateTextView.setText(comment.getDateTime().toString());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView authorTextView;
        public TextView contentTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tvCommentAuthor);
            contentTextView = itemView.findViewById(R.id.tvCommentContent);
            dateTextView = itemView.findViewById(R.id.tvCommentDateTime);
        }
    }
}
