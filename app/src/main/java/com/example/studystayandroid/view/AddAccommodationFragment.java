package com.example.studystayandroid.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.controller.AccommodationPhotoController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;
import com.example.studystayandroid.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AddAccommodationFragment extends Fragment {

    private static final int PICK_IMAGES_REQUEST = 1;

    private TextInputLayout textFieldAddress;
    private TextInputEditText addressEditText;
    private Spinner citySpinner;
    private TextInputLayout textFieldPrice;
    private TextInputEditText priceEditText;
    private TextInputLayout textFieldDescription;
    private TextInputEditText descriptionEditText;
    private TextInputLayout textFieldServices;
    private TextInputEditText servicesEditText;
    private Spinner capacitySpinner;
    private Button submitButton, uploadPhotosButton;

    private AccommodationController accommodationController;
    private List<Uri> selectedImagesUris;
    private User currentUser;


    public AddAccommodationFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_accommodation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textFieldAddress = view.findViewById(R.id.textFieldAddress);
        addressEditText = (TextInputEditText) textFieldAddress.getEditText();
        citySpinner = view.findViewById(R.id.city_spinner);
        textFieldPrice = view.findViewById(R.id.textFieldPrice);
        priceEditText = (TextInputEditText) textFieldPrice.getEditText();
        textFieldDescription = view.findViewById(R.id.textFieldDescription);
        descriptionEditText = (TextInputEditText) textFieldDescription.getEditText();
        textFieldServices = view.findViewById(R.id.textFieldServices);
        servicesEditText = (TextInputEditText) textFieldServices.getEditText();
        capacitySpinner = view.findViewById(R.id.capacity_spinner);
        submitButton = view.findViewById(R.id.submit_button);
        uploadPhotosButton = view.findViewById(R.id.show_reviews);

        accommodationController = new AccommodationController(requireContext());
        selectedImagesUris = new ArrayList<>();

        setupSpinners();

        uploadPhotosButton.setOnClickListener(v -> openImagePicker());
        submitButton.setOnClickListener(v -> submitAccommodation());

        // Retrieve currentUser from arguments
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("currentUser");
        }
    }

    private void setupSpinners() {
        // Set up city spinner with custom styles
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.cities_array,
                R.layout.spinner_item);  // Using custom layout

        cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);  // Using custom dropdown layout
        citySpinner.setAdapter(cityAdapter);

        // Set up capacity spinner with custom styles
        ArrayAdapter<CharSequence> capacityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.capacity_array,
                R.layout.spinner_item);  // Using custom layout

        capacityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);  // Using custom dropdown layout
        capacitySpinner.setAdapter(capacityAdapter);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && data != null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImagesUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImagesUris.add(imageUri);
            }
            Toast.makeText(getContext(), selectedImagesUris.size() + " images selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitAccommodation() {
        String address = addressEditText.getText().toString().trim();
        String city = citySpinner.getSelectedItem().toString();
        String price = priceEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        int capacity = Integer.parseInt(capacitySpinner.getSelectedItem().toString());
        String services = servicesEditText.getText().toString().trim();

        // Validate inputs
        if (!isValidAddress(address)) {
            Toast.makeText(getContext(), "Invalid address. Please enter a valid address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPrice(price)) {
            Toast.makeText(getContext(), "Invalid price. Please enter a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidText(description)) {
            Toast.makeText(getContext(), "Invalid description. Please avoid special characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidText(services)) {
            Toast.makeText(getContext(), "Invalid services. Please avoid special characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        Accommodation accommodation = new Accommodation();
        Log.d("Owner", currentUser.toString());
        accommodation.setOwner(currentUser);
        accommodation.setAddress(address);
        accommodation.setCity(city);
        accommodation.setPrice(new BigDecimal(price));
        accommodation.setDescription(description);
        accommodation.setCapacity(capacity);
        accommodation.setServices(services);
        accommodation.setAvailability(true);
        accommodation.setRating(0.0);

        accommodationController.createAccommodation(accommodation, new AccommodationController.SimpleCallback() {
            @Override
            public void onSuccess(Accommodation createdAccommodation) {
                uploadPhotos(createdAccommodation);
                Log.d("AddAccommodationFragment", "Accommodation created successfully");
                // Clear form or navigate back
                clearForm();
                Toast.makeText(getContext(), "Accommodation created successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(String error) {
                Log.e("AddAccommodationFragment", "Error creating accommodation: " + error);
                Toast.makeText(getContext(), "Error creating accommodation: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadPhotos(Accommodation accommodation) {
        AccommodationPhotoController photoController = new AccommodationPhotoController(requireContext());
        for (Uri uri : selectedImagesUris) {
            byte[] photoData = convertUriToByteArray(uri);
            AccommodationPhoto photo = new AccommodationPhoto(accommodation, photoData);
            photoController.createPhoto(photo, new AccommodationPhotoController.PhotoCallback() {
                @Override
                public void onSuccess(Object result) {
                    Log.d("AddAccommodationFragment", "Photo uploaded successfully");
                }

                @Override
                public void onError(String error) {
                    Log.e("AddAccommodationFragment", "Error uploading photo: " + error);
                }
            });
        }
    }

    private byte[] convertUriToByteArray(Uri uri) {
        try (InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.e("AddAccommodationFragment", "Error converting URI to byte array", e);
            return new byte[0];
        }
    }

    // Validation methods
    private boolean isValidAddress(String address) {
        // Address should be street followed by number, e.g., "Street 123"
        String addressPattern = "^[a-zA-Z0-9\\s]+(\\s\\d+)?$";
        return Pattern.matches(addressPattern, address);
    }

    private boolean isValidPrice(String price) {
        try {
            new BigDecimal(price);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidText(String text) {
        // No special characters allowed
        String textPattern = "^[a-zA-Z0-9\\s]+$";
        return Pattern.matches(textPattern, text);
    }

    private void clearForm() {
        addressEditText.setText("");
        priceEditText.setText("");
        descriptionEditText.setText("");
        servicesEditText.setText("");
        selectedImagesUris.clear();
    }
}