package com.example.studystayandroid.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.User;
import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private User currentUser;
    private UserController userController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        userController = new UserController(this);
        currentUser = new User();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setStatusBarColor(getResources().getColor(R.color.statusBarColor));
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_profile);
        }

        loadUserDetails();
    }

    private void loadUserDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userId", -1);

        if (userId != -1) {
            userController.getUserById(userId, new UserController.UserCallback() {
                @Override
                public void onSuccess(Object result) {
                    currentUser = (User) result;
                    updateUI();
                }

                @Override
                public void onSuccess(User author) {
                    currentUser = author;
                    updateUI();
                }

                @Override
                public void onError(String error) {
                    Log.e("Dashboard", "Error fetching user details: " + error);
                }
            });
        } else {
            Log.e("Dashboard", "User ID not found in SharedPreferences");
        }
    }

    private void updateUI() {
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.nav_user_name);
        TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        navUserName.setText(currentUser.getName() + " " + currentUser.getLastName());
        navUserEmail.setText(currentUser.getEmail());

        byte[] userPhotoBytes = currentUser.getProfilePicture();
        if (userPhotoBytes != null && userPhotoBytes.length > 0) {
            Glide.with(this)
                    .asBitmap()
                    .load(userPhotoBytes)
                    .transform(new CircleCrop())
                    .into(navUserPhoto);
        } else {
            Glide.with(this)
                    .load(R.drawable.defaultprofile)
                    .transform(new CircleCrop())
                    .into(navUserPhoto);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser); // Pass the currentUser object

        switch (item.getItemId()) {
            case R.id.Accommodations:
                selectedFragment = new AccommodationsFragment();
                break;
            case R.id.nav_chat:
                selectedFragment = new ChatFragment();
                break;
            case R.id.nav_profile:
                selectedFragment = new ProfileFragment();
                break;
            case R.id.nav_forum:
                selectedFragment = new ForumFragment();
                break;
            case R.id.nav_logout:
                showLogoutConfirmationDialog();
                break;
            case R.id.nav_closeapp:
                showExitConfirmationDialog();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }

        if (selectedFragment != null) {
            selectedFragment.setArguments(bundle); // Set the arguments to the fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirmation, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Confirm Logout");
        dialogMessage.setText("Are you sure you want to log out?");
        buttonConfirm.setText("Yes");

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            logout();
            dialog.dismiss();
        });

        dialog.show();
    }


    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.apply();

        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirmation, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Confirm Exit");
        dialogMessage.setText("Are you sure you want to exit the app?");
        buttonConfirm.setText("Yes");

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
            finish();
            System.exit(0);
        });

        dialog.show();
    }



    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
