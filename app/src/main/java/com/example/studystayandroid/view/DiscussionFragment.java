package com.example.studystayandroid.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ForumCommentController;
import com.example.studystayandroid.model.ForumComment;
import com.example.studystayandroid.model.ForumTopic;
import com.example.studystayandroid.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DiscussionFragment extends Fragment {

    private static final String ARG_TOPIC = "arg_topic";

    private ForumTopic topic;
    private TextView topicTitleTextView;
    private TextView descriptionTextView;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private ImageButton button3;
    private Button buttonAddComment;
    private ForumCommentController forumCommentController;
    private List<ForumComment> commentList = new ArrayList<>();
    private User currentUser;

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

        currentUser = getCurrentUser();
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

        button3 = view.findViewById(R.id.button3);

        descriptionTextView = view.findViewById(R.id.descriptionTopic);
        descriptionTextView.setText(topic.getDescription());

        buttonAddComment = view.findViewById(R.id.addComment);

        // Configurar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(getContext(), commentList, currentUser, this);
        recyclerView.setAdapter(commentAdapter);

        button3.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        buttonAddComment.setOnClickListener(v -> showNewCommentDialog());

        // Cargar los comentarios
        loadComments();
    }

    void loadComments() {
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

    private void showNewCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_comment, null);
        builder.setView(dialogView);

        EditText editTextContent = dialogView.findViewById(R.id.editTextCommentContent);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonPost = dialogView.findViewById(R.id.buttonPost);

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonPost.setOnClickListener(v -> {
            String content = editTextContent.getText().toString();

            if (content.isEmpty()) {
                // Manejar contenido vacío
                AlertDialog.Builder errorBuilder = new AlertDialog.Builder(getContext());
                LayoutInflater errorInflater = getLayoutInflater();
                View errorDialogView = errorInflater.inflate(R.layout.dialog_error, null);
                errorBuilder.setView(errorDialogView);

                TextView errorTitle = errorDialogView.findViewById(R.id.dialogTitle);
                TextView errorMessage = errorDialogView.findViewById(R.id.dialogMessage);
                Button errorButton = errorDialogView.findViewById(R.id.buttonConfirm);

                errorTitle.setText("Error");
                errorMessage.setText("Content must be provided.");

                AlertDialog errorDialog = errorBuilder.create();
                errorButton.setOnClickListener(ev -> errorDialog.dismiss());
                errorDialog.show();
                return;
            }

            // Crear y guardar el nuevo comentario
            ForumComment newComment = new ForumComment();
            newComment.setContent(content);
            newComment.setAuthor(currentUser);
            newComment.setTopic(topic);
            newComment.setDateTime(LocalDateTime.now());

            forumCommentController.createComment(newComment, new ForumCommentController.CommentCallback() {
                @Override
                public void onSuccess(ForumComment comment) {
                    loadComments(); // Recargar los comentarios
                    Log.d("DiscussionFragment", "Comment created: " + comment);
                    dialog.dismiss();
                }

                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onError(String error) {
                    Log.e("DiscussionFragment", "Error creating comment: " + error);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private User getCurrentUser() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        User user = new User();
        user.setUserId(sharedPreferences.getLong("userId", -1));
        user.setName(sharedPreferences.getString("userName", ""));
        user.setLastName(sharedPreferences.getString("userLastName", ""));
        user.setEmail(sharedPreferences.getString("userEmail", ""));
        // Si hay más campos necesarios, añádelos aquí
        return user;
    }

    public void openStrangeProfile(User user) {
        StrangeProfileFragment strangeProfileFragment = StrangeProfileFragment.newInstance(user);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, strangeProfileFragment)
                .addToBackStack(null)
                .commit();
    }
}
