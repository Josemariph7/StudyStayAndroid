package com.example.studystayandroid.view;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private User currentUser;
    private UserController userController;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView birthDateTextView;
    private TextView registerDateTextView;
    private TextView dniTextView;
    private ImageView profilePhoto;
    private Button contactButton;
    private NavigationView navigationView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        nameTextView = view.findViewById(R.id.nameTextViewProfile);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        birthDateTextView = view.findViewById(R.id.birthDateTextView);
        registerDateTextView = view.findViewById(R.id.registerDateTextView);
        dniTextView = view.findViewById(R.id.dniTextView);
        profilePhoto = view.findViewById(R.id.profilePhoto);
        contactButton = view.findViewById(R.id.ContactButton);

        // Configurar TabLayout y ViewPager2
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Rented");
                    break;
                case 1:
                    tab.setText("Listed");
                    break;
            }
        }).attach();

        // Obtener el usuario actual de SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userId", -1);
        if (userId == -1) {
            Log.e("ProfileFragment", "Error: Usuario no autenticado.");
            return;
        }

        // Inicializar UserController
        userController = new UserController(getActivity());
        userController.getUserById(userId, new UserController.UserCallback() {
            @Override
            public void onSuccess(Object result) {
                currentUser = (User) result;
                updateProfileUI();
                adapter.setUser(currentUser); // Pasar el usuario al adaptador del ViewPager
            }

            @Override
            public void onSuccess(User user) {
                currentUser = user;
                updateProfileUI();
                adapter.setUser(currentUser); // Pasar el usuario al adaptador del ViewPager
            }

            @Override
            public void onError(String error) {
                Log.e("ProfileFragment", "Error al cargar el usuario: " + error);
            }
        });

        contactButton.setOnClickListener(v -> showProfileSettingsDialog());
    }


    private void updateProfileUI() {
        if (currentUser != null) {
            nameTextView.setText(currentUser.getName() + " " + currentUser.getLastName());
            emailTextView.setText(currentUser.getEmail());
            phoneTextView.setText(currentUser.getPhone());
            birthDateTextView.setText(currentUser.getBirthDate() != null ? currentUser.getBirthDate().toString() : "N/A");

            if (currentUser.getRegistrationDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedRegisterDate = currentUser.getRegistrationDate().format(formatter);
                registerDateTextView.setText(formattedRegisterDate);
            } else {
                registerDateTextView.setText("N/A");
            }

            dniTextView.setText(currentUser.getDni());
            if (currentUser.getProfilePicture() != null) {
                Glide.with(this)
                        .load(currentUser.getProfilePicture())
                        .transform(new CircleCrop())
                        .into(profilePhoto);
            } else {
                Glide.with(this)
                        .load(R.drawable.defaultprofile)
                        .transform(new CircleCrop())
                        .into(profilePhoto);
            }
        }
    }

    private void showProfileSettingsDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_settings, null);
        AlertDialog settingsDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button changeProfilePictureButton = dialogView.findViewById(R.id.changeProfilePictureButton);
        Button changePasswordButton = dialogView.findViewById(R.id.changePasswordButton);
        Button updateUserInfoButton = dialogView.findViewById(R.id.updateUserInfoButton);
        Button deleteAccountButton = dialogView.findViewById(R.id.deleteAccountButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        changeProfilePictureButton.setOnClickListener(v -> {
            settingsDialog.dismiss();
            changeProfilePicture();
        });

        changePasswordButton.setOnClickListener(v -> {
            settingsDialog.dismiss();
            changePassword();
        });

        updateUserInfoButton.setOnClickListener(v -> {
            settingsDialog.dismiss();
            updateUserInfo();
        });

        deleteAccountButton.setOnClickListener(v -> {
            settingsDialog.dismiss();
            deleteAccount();
        });

        cancelButton.setOnClickListener(v -> settingsDialog.dismiss());

        settingsDialog.show();
    }



    private void changeProfilePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void changePassword() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);
        AlertDialog changePasswordDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        EditText currentPasswordEditText = dialogView.findViewById(R.id.currentPasswordEditText);
        EditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        buttonCancel.setOnClickListener(v -> changePasswordDialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            String currentPassword = currentPasswordEditText.getText().toString();
            String newPassword = newPasswordEditText.getText().toString();

            if (currentUser.getPassword() == null || !currentUser.getPassword().equals(currentPassword)) {
                showErrorDialog("Current password is incorrect");
            } else if (newPassword.length() < 6) {
                showErrorDialog("New password must be at least 6 characters long");
            } else {
                userController.updateUserPassword(currentUser.getUserId(), newPassword, new UserController.UserCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        changePasswordDialog.dismiss();
                        showSuccessDialog("Password changed successfully");
                    }

                    @Override
                    public void onSuccess(User user) {
                        changePasswordDialog.dismiss();
                        showSuccessDialog("Password changed successfully");
                    }

                    @Override
                    public void onError(String error) {
                        showErrorDialog("Error changing password: " + error);
                    }
                });
            }
        });

        changePasswordDialog.show();
    }

    private void updateUserInfo() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_user_info, null);
        AlertDialog updateUserInfoDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText lastNameEditText = dialogView.findViewById(R.id.lastNameEditText);
        EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        EditText phoneEditText = dialogView.findViewById(R.id.phoneEditText);
        EditText bioEditText = dialogView.findViewById(R.id.bioEditText);
        Spinner genderSpinner = dialogView.findViewById(R.id.genderSpinner);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        nameEditText.setText(currentUser.getName());
        lastNameEditText.setText(currentUser.getLastName());
        emailEditText.setText(currentUser.getEmail());
        phoneEditText.setText(currentUser.getPhone());
        bioEditText.setText(currentUser.getBio() != null ? currentUser.getBio() : "");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        if (currentUser.getGender() != null) {
            int spinnerPosition = adapter.getPosition(currentUser.getGender().name());
            genderSpinner.setSelection(spinnerPosition);
        }

        buttonCancel.setOnClickListener(v -> updateUserInfoDialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            currentUser.setName(nameEditText.getText().toString());
            currentUser.setLastName(lastNameEditText.getText().toString());
            currentUser.setEmail(emailEditText.getText().toString());
            currentUser.setPhone(phoneEditText.getText().toString());
            currentUser.setBio(bioEditText.getText().toString());
            currentUser.setGender(User.Gender.valueOf(genderSpinner.getSelectedItem().toString().toUpperCase()));

            userController.updateUser(currentUser, new UserController.UserCallback() {
                @Override
                public void onSuccess(Object result) {
                    updateProfileUI();
                    updateUserInfoDialog.dismiss();
                    showSuccessDialog("User info updated successfully");
                }

                @Override
                public void onSuccess(User user) {
                    updateProfileUI();
                    updateUserInfoDialog.dismiss();
                    showSuccessDialog("User info updated successfully");
                }

                @Override
                public void onError(String error) {
                    showErrorDialog("Error updating user info: " + error);
                }
            });
        });

        updateUserInfoDialog.show();
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuccessDialog(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void deleteAccount() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_account, null);
        AlertDialog deleteAccountDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        buttonCancel.setOnClickListener(v -> deleteAccountDialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString();

            if (currentUser.getPassword().equals(password)) {
                userController.deleteUser(currentUser.getUserId(), new UserController.UserCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("userId");
                        editor.apply();

                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }

                    @Override
                    public void onSuccess(User user) {
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("userId");
                        editor.apply();

                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }

                    @Override
                    public void onError(String error) {
                        showErrorDialog("Error deleting account: " + error);
                    }
                });
            } else {
                showErrorDialog("Password is incorrect");
            }
        });

        deleteAccountDialog.show();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                userController.updateUserProfilePicture(currentUser.getUserId(), imageBytes, new UserController.UserCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.d("ProfileFragment", "Profile picture updated successfully.");
                        Glide.with(ProfileFragment.this)
                                .load(imageUri)
                                .transform(new CircleCrop())
                                .into(profilePhoto);

                        View headerView = navigationView.getHeaderView(0);
                        ImageView navImageView = headerView.findViewById(R.id.nav_user_photo);
                        Glide.with(ProfileFragment.this)
                                .load(imageUri)
                                .transform(new CircleCrop())
                                .into(navImageView);
                    }

                    @Override
                    public void onSuccess(User user) {
                        Log.d("ProfileFragment", "Profile picture updated successfully.");
                        Glide.with(ProfileFragment.this)
                                .load(imageUri)
                                .transform(new CircleCrop())
                                .into(profilePhoto);

                        View headerView = navigationView.getHeaderView(0);
                        ImageView navImageView = headerView.findViewById(R.id.nav_user_photo);
                        Glide.with(ProfileFragment.this)
                                .load(imageUri)
                                .transform(new CircleCrop())
                                .into(navImageView);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("ProfileFragment", "Error updating profile picture: " + error);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
