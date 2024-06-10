package com.example.studystayandroid.view;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
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
    private Button submitButton, uploadPhotosButton, backButton;

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
        uploadPhotosButton = view.findViewById(R.id.upload_photos_button);
        backButton = view.findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        accommodationController = new AccommodationController(requireContext());
        selectedImagesUris = new ArrayList<>();

        setupSpinners();

        uploadPhotosButton.setOnClickListener(v -> openImagePicker());
        submitButton.setOnClickListener(v -> submitAccommodation());

        submitButton.setOnClickListener(v -> {
            submitButton.setEnabled(false);
            submitAccommodation();
        });

        // Retrieve currentUser from arguments
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("currentUser");
            Log.d("AddAccommodationFragment", "Current user: " + currentUser);
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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
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
        String city = citySpinner.getSelectedItem() != null ? citySpinner.getSelectedItem().toString() : "All";
        String price = priceEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String capacityStr = capacitySpinner.getSelectedItem() != null ? capacitySpinner.getSelectedItem().toString() : "All";
        int capacity = capacityStr.equals("All") ? -1 : Integer.parseInt(capacityStr);
        String services = servicesEditText.getText().toString().trim();

        boolean isValid = validateInputs(address, city, price, description, capacity, services);

        if (isValid) {
            // Verificar si el alojamiento ya existe
            accommodationController.getAccommodations(new AccommodationController.AccommodationListCallback() {
                @Override
                public void onSuccess(List<Accommodation> accommodations) {
                    for (Accommodation existingAccommodation : accommodations) {
                        if (existingAccommodation.getOwner().getUserId().equals(currentUser.getUserId()) &&
                                existingAccommodation.getAddress().equalsIgnoreCase(address)) {
                            Toast.makeText(getContext(), "Accommodation already exists at this address.", Toast.LENGTH_SHORT).show();
                            submitButton.setEnabled(true);  // Rehabilitar el botón de envío
                            return;
                        }
                    }

                    // Crear nuevo alojamiento si no existe duplicado
                    Accommodation accommodation = new Accommodation();
                    accommodation.setOwner(currentUser);
                    accommodation.setAddress(address);
                    accommodation.setCity(city);
                    accommodation.setPrice(new BigDecimal(price));
                    accommodation.setDescription(description);
                    accommodation.setCapacity(capacity);
                    accommodation.setServices(services);
                    accommodation.setAvailability(true);
                    accommodation.setRating(0.0);

                    accommodationController.createAccommodation(accommodation, new AccommodationController.AccommodationCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            Accommodation createdAccommodation = (Accommodation) result;
                            if (selectedImagesUris.isEmpty()) {
                                clearForm();
                                navigateBackWithSuccess();
                                Toast.makeText(getContext(), "Accommodation created successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                uploadPhotos(createdAccommodation);
                            }
                            submitButton.setEnabled(true);  // Rehabilitar el botón de envío
                        }

                        @Override
                        public void onSuccess(Accommodation createdAccommodation) {
                            if (selectedImagesUris.isEmpty()) {
                                clearForm();
                                navigateBackWithSuccess();
                                Toast.makeText(getContext(), "Accommodation created successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                uploadPhotos(createdAccommodation);
                            }
                            submitButton.setEnabled(true);  // Rehabilitar el botón de envío
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("AddAccommodationFragment", "Error creating accommodation: " + error);
                            Toast.makeText(getContext(), "Error creating accommodation: " + error, Toast.LENGTH_SHORT).show();
                            submitButton.setEnabled(true);  // Rehabilitar el botón de envío
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e("AddAccommodationFragment", "Error checking accommodations: " + error);
                    submitButton.setEnabled(true);  // Rehabilitar el botón de envío
                }
            });
        } else {
            submitButton.setEnabled(true);  // Rehabilitar el botón de envío si hay errores de validación
        }
    }


    private boolean validateInputs(String address, String city, String price, String description, int capacity, String services) {
        boolean isValid = true;

        if (address.isEmpty() || !Pattern.matches("^[a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ,]+$", address)) {
            textFieldAddress.setError("Invalid address. Please enter a valid address.");
            isValid = false;
        } else {
            textFieldAddress.setError(null);
        }

        if (city.equals("All")) {
            Toast.makeText(getContext(), "Please select a city.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        try {
            BigDecimal priceDecimal = new BigDecimal(price);
            if (priceDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                textFieldPrice.setError("Price must be greater than 0.");
                isValid = false;
            } else {
                textFieldPrice.setError(null);
            }
        } catch (NumberFormatException e) {
            textFieldPrice.setError("Invalid price. Please enter a valid number.");
            isValid = false;
        }

        if (description.isEmpty() || !Pattern.matches("^[a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ,]+$", description)) {
            textFieldDescription.setError("Invalid description. Please avoid special characters.");
            isValid = false;
        } else {
            textFieldDescription.setError(null);
        }

        if (services.isEmpty() || !Pattern.matches("^[a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ,]+$", services)) {
            textFieldServices.setError("Invalid services. Please avoid special characters.");
            isValid = false;
        } else {
            textFieldServices.setError(null);
        }

        if (capacity <= 0) {
            Toast.makeText(getContext(), "Please select a valid capacity.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void uploadPhotos(Accommodation accommodation) {
        AccommodationPhotoController photoController = new AccommodationPhotoController(requireContext());

        for (Uri uri : selectedImagesUris) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // Adjust compression as needed
                byte[] photoData = byteArrayOutputStream.toByteArray();

                AccommodationPhoto photo = new AccommodationPhoto(accommodation, photoData);
                photoController.createPhoto(photo, new AccommodationPhotoController.PhotoCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.d("AddAccommodationFragment", "Photo uploaded successfully");
                        if (selectedImagesUris.indexOf(uri) == selectedImagesUris.size() - 1) {
                            clearForm();
                            navigateBackWithSuccess();
                            Toast.makeText(getContext(), "Accommodation and photos created successfully", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("AddAccommodationFragment", "Error uploading photo: " + error);
                        Toast.makeText(getContext(), "Error uploading photo: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                Log.e("AddAccommodationFragment", "Error processing image", e);
            }
        }
    }

    private void navigateBackWithSuccess() {
        getParentFragmentManager().popBackStack();
        // Notify the AccommodationsFragment to show success dialog
        getParentFragmentManager().setFragmentResult("accommodationResult", new Bundle());
    }

    private void clearForm() {
        addressEditText.setText("");
        priceEditText.setText("");
        descriptionEditText.setText("");
        servicesEditText.setText("");
        citySpinner.setSelection(0);
        capacitySpinner.setSelection(0);
        selectedImagesUris.clear();
    }
}
