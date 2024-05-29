package com.example.studystayandroid.view;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

        // Cambiar el título de la ActionBar
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Profile");
        }

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

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Rented");
                        break;
                    case 1:
                        tab.setText("Listed");
                        break;
                }
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
                User user = (User) result;
                Glide.with(ProfileFragment.this)
                        .load(R.drawable.defaultprofile)
                        .transform(new CircleCrop())
                        .into(profilePhoto);
                currentUser = user;
                updateProfileUI();
            }

            @Override
            public void onSuccess(User user) {
                currentUser = user;
                Glide.with(ProfileFragment.this)
                        .load(R.drawable.defaultprofile)
                        .transform(new CircleCrop())
                        .into(profilePhoto);
                updateProfileUI();
            }

            @Override
            public void onError(String error) {
                Log.e("ProfileFragment", "Error al cargar el usuario: " + error);
            }
        });
        contactButton.setOnClickListener(v -> showProfileSettingsDialog());
    }

    // Dentro de tu método updateProfileUI()
    private void updateProfileUI() {
        if (currentUser != null) {
            nameTextView.setText(currentUser.getName() + " " + currentUser.getLastName());
            emailTextView.setText(currentUser.getEmail());
            phoneTextView.setText(currentUser.getPhone());
            birthDateTextView.setText(currentUser.getBirthDate() != null ? currentUser.getBirthDate().toString() : "N/A");

            // Formatear la fecha de registro
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
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Account Settings")
                .setItems(new String[]{"Change Profile Picture", "Change Password", "Delete Account"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            changeProfilePicture();
                            break;
                        case 1:
                            changePassword();
                            break;
                        case 2:
                            deleteAccount();
                            break;
                    }
                })
                .show();
    }

    private void changeProfilePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void changePassword() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_change_password, null);
        EditText currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText);
        EditText newPasswordEditText = view.findViewById(R.id.newPasswordEditText);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change Password")
                .setView(view)
                .setPositiveButton("Change", (dialog, which) -> {
                    String currentPassword = currentPasswordEditText.getText().toString();
                    String newPassword = newPasswordEditText.getText().toString();

                    if (currentUser.getPassword().equals(currentPassword)) {
                        currentUser.setPassword(newPassword);
                        userController.updateUser(currentUser, new UserController.UserCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(User user) {
                                // No se usa
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Error changing password: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_delete_account, null);
        EditText passwordEditText = view.findViewById(R.id.passwordEditText);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setView(view)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String password = passwordEditText.getText().toString();

                    if (currentUser.getPassword().equals(password)) {
                        userController.deleteUser(currentUser.getUserId(), new UserController.UserCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                // Clear the user ID from shared preferences
                                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("userId");
                                editor.apply();

                                // Redirect to MainActivity
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

                                // Redirect to MainActivity
                                Intent intent = new Intent(requireContext(), MainActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Error deleting account: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), "Password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Subir la imagen codificada a la base de datos
                userController.updateUserProfilePicture(currentUser.getUserId(), encodedImage, new UserController.UserCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.d("ProfileFragment", "Profile picture updated successfully.");
                        // Actualizar la imagen en la interfaz de usuario
                        Glide.with(ProfileFragment.this)
                                .load(imageUri)
                                .transform(new CircleCrop())
                                .into(profilePhoto);
                    }

                    @Override
                    public void onSuccess(User user) {
                        Log.d("ProfileFragment", "Profile picture updated successfully.");
                        // Actualizar la imagen en la interfaz de usuario
                        Glide.with(ProfileFragment.this)
                                .load(imageUri)
                                .transform(new CircleCrop())
                                .into(profilePhoto);
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
