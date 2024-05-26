package com.example.studystayandroid.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.controller.ForumCommentController;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.ForumComment;
import com.example.studystayandroid.model.ForumTopic;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiscussionFragment extends Fragment {

    private static final String ARG_TOPIC = "arg_topic";

    private ForumTopic topic;
    private TextView topicTitleTextView;
    private TextView descriptionTextView;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private ForumCommentController forumCommentController;
    private List<ForumComment> commentList = new ArrayList<>();

    public static DiscussionFragment newInstance(ForumTopic topic) {
        DiscussionFragment fragment = new DiscussionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TOPIC, topic);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            topic = (ForumTopic) getArguments().getSerializable(ARG_TOPIC);
        }
        forumCommentController = new ForumCommentController(getContext());
        Log.d("DiscussionFragment", "onCreate: Topic loaded: " + topic);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cambiar el título de la ActionBar
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Discussion");
        }

        // Configurar el título del tema
        topicTitleTextView = view.findViewById(R.id.tvDiscussionTitle);
        topicTitleTextView.setText(topic.getTitle());

        descriptionTextView = view.findViewById(R.id.descriptionTopic);
        descriptionTextView.setText(topic.getDescription());

        // Configurar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(commentList);
        recyclerView.setAdapter(commentAdapter);

        // Cargar los comentarios
        loadComments();
    }

    private void loadComments() {
        Log.d("DiscussionFragment", "loadComments: Loading comments for topic ID: " + topic.getTopicId());
        Log.d("DiscussionFragment", topic.toString());
        forumCommentController.getComments(topic.getTopicId(), new ForumCommentController.CommentListCallback() {
            @Override
            public void onSuccess(List<ForumComment> comments) {
                commentList.clear();
                Log.d("DiscussionFragment", "onSuccess: Comments loaded: " + comments.size());
                for (ForumComment comment : comments) {
                    if (comment.getTopic().getTopicId().equals(topic.getTopicId())) {
                        commentList.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
                Log.d("DiscussionFragment", "onSuccess: Comments displayed: " + commentList.size());
            }

            @Override
            public void onError(String error) {
                // Manejar el error
                Log.e("DiscussionFragment", "Error fetching comments: " + error);
            }
        });
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

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
            holder.commentTextView.setText(comment.getContent());
            holder.authorTextView.setText(comment.getAuthor().getName() + " " + comment.getAuthor().getLastName());
            holder.dateTimeTextView.setText(comment.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView commentTextView;
            public TextView authorTextView;
            public TextView dateTimeTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                commentTextView = itemView.findViewById(R.id.tvCommentContent);
                authorTextView = itemView.findViewById(R.id.tvCommentAuthor);
                dateTimeTextView = itemView.findViewById(R.id.tvCommentDateTime);
            }
        }
    }
}
