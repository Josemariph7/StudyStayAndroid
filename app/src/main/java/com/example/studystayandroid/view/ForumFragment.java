package com.example.studystayandroid.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.ForumTopicController;
import com.example.studystayandroid.model.ForumTopic;
import com.example.studystayandroid.model.User;

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
        forumTopicController.getTopics(new ForumTopicController.TopicListCallback() {
            @Override
            public void onSuccess(List<ForumTopic> topics) {
                topicsList.clear();
                Log.d("ForumFragment", "Received topics: " + topics);
                topicsList.addAll(topics);
                forumTopicAdapter = new ForumTopicAdapter(topicsList, currentUser, topic -> openDiscussionFragment(topic));
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
}
