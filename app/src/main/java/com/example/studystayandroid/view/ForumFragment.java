package com.example.studystayandroid.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ForumTopicController;
import com.example.studystayandroid.model.ForumTopic;
import com.example.studystayandroid.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ForumFragment extends Fragment {

    private List<ForumTopic> topicsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ForumTopicAdapter forumTopicAdapter;
    private ForumTopicController forumTopicController;
    private User currentUser;

    public ForumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cambiar el título de la ActionBar
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Forum");
        }

        recyclerView = view.findViewById(R.id.recyclerViewForumTopics);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setBackgroundColor(getResources().getColor(android.R.color.white));

        // Agregar DividerItemDecoration
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.divider);
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Obtener el usuario actual de SharedPreferences
        currentUser = getCurrentUser();

        forumTopicController = new ForumTopicController(getContext());
        loadTopics();

        // Configurar el botón para abrir el diálogo
        Button buttonAddTopic = view.findViewById(R.id.button2);
        buttonAddTopic.setOnClickListener(v -> showNewTopicDialog());
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

    private void openDiscussionFragment(ForumTopic topic) {
        DiscussionFragment discussionFragment = DiscussionFragment.newInstance(topic);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, discussionFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showNewTopicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_topic, null);
        builder.setView(dialogView);

        EditText editTextTitle = dialogView.findViewById(R.id.editTextTopicTitle);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextTopicDescription);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonPost = dialogView.findViewById(R.id.buttonPost);

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonPost.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();

            if (title.isEmpty() || description.isEmpty()) {
                // Manejar campos vacíos
                AlertDialog.Builder errorBuilder = new AlertDialog.Builder(getContext());
                LayoutInflater errorInflater = getLayoutInflater();
                View errorDialogView = errorInflater.inflate(R.layout.dialog_error, null);
                errorBuilder.setView(errorDialogView);

                TextView errorTitle = errorDialogView.findViewById(R.id.dialogTitle);
                TextView errorMessage = errorDialogView.findViewById(R.id.dialogMessage);
                Button errorButton = errorDialogView.findViewById(R.id.buttonConfirm);

                errorTitle.setText("Error");
                errorMessage.setText("Both title and description must be provided.");

                AlertDialog errorDialog = errorBuilder.create();
                errorButton.setOnClickListener(ev -> errorDialog.dismiss());
                errorDialog.show();
                return;
            }

            // Crear y guardar el nuevo tema
            ForumTopic newTopic = new ForumTopic();
            newTopic.setTitle(title);
            newTopic.setDescription(description);
            newTopic.setAuthor(currentUser);
            newTopic.setDateTime(LocalDateTime.now());

            Log.d("ForumFragment", "New Topic: " + newTopic);

            forumTopicController.createTopic(newTopic, new ForumTopicController.TopicCallback() {
                @Override
                public void onSuccess(ForumTopic topic) {
                    if (topic != null) {
                        reloadTopics();
                        Log.d("ForumFragment", "Topic created: " + topic);
                    } else {
                        reloadTopics();
                        Log.e("ForumFragment", "Created topic is null");
                    }
                    dialog.dismiss();
                }

                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onError(String error) {
                    Log.e("ForumFragment", "Error creating topic: " + error);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void loadTopics() {
        forumTopicController.getTopics(new ForumTopicController.TopicListCallback() {
            @Override
            public void onSuccess(List<ForumTopic> topics) {
                topicsList.clear();
                Log.d("ForumFragment", "Received topics: " + topics);
                topicsList.addAll(topics);
                forumTopicAdapter = new ForumTopicAdapter(topicsList, currentUser, topic -> openDiscussionFragment(topic), ForumFragment.this);
                recyclerView.setAdapter(forumTopicAdapter);
                forumTopicAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                // Handle error
                Log.e("ForumFragment", "Error fetching topics: " + error);
            }
        });
    }

    public void openStrangeProfile(User user) {
        StrangeProfileFragment strangeProfileFragment = StrangeProfileFragment.newInstance(user);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, strangeProfileFragment)
                .addToBackStack(null)
                .commit();
    }

    public void reloadTopics() {
        loadTopics();
    }
}
