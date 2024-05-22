package com.example.studystayandroid.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
        userController=new UserController(this);
        currentUser=new User();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
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
                    User user = (User) result;

                    View headerView = navigationView.getHeaderView(0);
                    TextView navUserName = headerView.findViewById(R.id.nav_user_name);
                    TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
                    ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

                    navUserName.setText(user.getName()+" "+user.getLastName());
                    navUserEmail.setText(user.getEmail());

                    byte[] userPhotoBytes = user.getProfilePicture();
                    if (userPhotoBytes != null) {
                        String userPhotoBase64 = Base64.encodeToString(userPhotoBytes, Base64.DEFAULT);
                        byte[] decodedString = Base64.decode(userPhotoBase64, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        navUserPhoto.setImageBitmap(decodedByte);
                    } else {
                        navUserPhoto.setImageResource(R.drawable.defaultprofile);
                    }
                }

                @Override
                public void onSuccess(User author) {
                    User user = (User) author;

                    View headerView = navigationView.getHeaderView(0);
                    TextView navUserName = headerView.findViewById(R.id.nav_user_name);
                    TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
                    ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

                    navUserName.setText(user.getName()+" "+user.getLastName());
                    navUserEmail.setText(user.getEmail());

                    byte[] userPhotoBytes = user.getProfilePicture();
                    if (userPhotoBytes != null) {
                        String userPhotoBase64 = Base64.encodeToString(userPhotoBytes, Base64.DEFAULT);
                        byte[] decodedString = Base64.decode(userPhotoBase64, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        navUserPhoto.setImageBitmap(decodedByte);
                    } else {
                        navUserPhoto.setImageResource(R.drawable.defaultprofile);
                    }
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (item.getItemId() == R.id.nav_chat) {
            selectedFragment = new ChatFragment();
        } else if (item.getItemId() == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (item.getItemId() == R.id.nav_forum) {
            selectedFragment = new ForumFragment();
        } else if (item.getItemId() == R.id.nav_logout) {
            Log.d("Dashboard", "User logged out");
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else {
            throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
