/*
 * StudyStay © 2024
 *
 * All rights reserved.
 *
 * This software and associated documentation files (the "Software") are owned by StudyStay. Unauthorized copying, distribution, or modification of this Software is strictly prohibited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this Software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * StudyStay
 * José María Pozo Hidalgo
 * Email: josemariph7@gmail.com
 *
 *
 */

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

/**
 * Fragmento para agregar un nuevo alojamiento.
 */
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

    /**
     * Constructor público y vacío requerido.
     */
    public AddAccommodationFragment() {
        // Constructor vacío requerido
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
        submitButton.setOnClickListener(v -> {
            submitButton.setEnabled(false);
            submitAccommodation();
        });

        // Recuperar el usuario actual de los argumentos
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("currentUser");
            Log.d("AddAccommodationFragment", "Current user: " + currentUser);
        }
    }

    /**
     * Configura los spinners de ciudad y capacidad con estilos personalizados.
     */
    private void setupSpinners() {
        // Configurar el spinner de ciudad con estilos personalizados
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.cities_array,
                R.layout.spinner_item);  // Usando diseño personalizado

        cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);  // Usando diseño desplegable personalizado
        citySpinner.setAdapter(cityAdapter);

        // Configurar el spinner de capacidad con estilos personalizados
        ArrayAdapter<CharSequence> capacityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.capacity_array,
                R.layout.spinner_item);  // Usando diseño personalizado

        capacityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);  // Usando diseño desplegable personalizado
        capacitySpinner.setAdapter(capacityAdapter);
    }

    /**
     * Abre el selector de imágenes para seleccionar fotos del alojamiento.
     */
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

    /**
     * Envía los detalles del alojamiento al servidor.
     */
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
                    } else {
                        uploadPhotos(createdAccommodation);
                    }
                    submitButton.setEnabled(true);  // Rehabilitar el botón de envío
                }

                @Override
                public void onError(String error) {
                    Log.e("AddAccommodationFragment", "Error creating accommodation: " + error);
                    submitButton.setEnabled(true);  // Rehabilitar el botón de envío
                }
            });
        } else {
            submitButton.setEnabled(true);  // Rehabilitar el botón de envío si hay errores de validación
        }
    }


    /**
     * Valida los datos de entrada del usuario.
     *
     * @param address     La dirección del alojamiento.
     * @param city        La ciudad del alojamiento.
     * @param price       El precio del alojamiento.
     * @param description La descripción del alojamiento.
     * @param capacity    La capacidad del alojamiento.
     * @param services    Los servicios proporcionados por el alojamiento.
     * @return true si todos los datos de entrada son válidos, false en caso contrario.
     */
    private boolean validateInputs(String address, String city, String price, String description, int capacity, String services) {
        boolean isValid = true;

        if (address.isEmpty() || !Pattern.matches("^[a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑ,]+$", address)) {
            textFieldAddress.setError("Invalid address. Please enter a valid address.");
            isValid = false;
        } else {
            textFieldAddress.setError(null);
        }

        if (city.equals("All")) {
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
            isValid = false;
        }

        return isValid;
    }

    /**
     * Sube las fotos seleccionadas del alojamiento.
     *
     * @param accommodation El alojamiento para subir las fotos.
     */
    private void uploadPhotos(Accommodation accommodation) {
        AccommodationPhotoController photoController = new AccommodationPhotoController(requireContext());
        Log.d("AddAccommodationFragment", "Accommodation: " + accommodation);
        for (Uri uri : selectedImagesUris) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // Ajustar la compresión según sea necesario
                byte[] photoData = byteArrayOutputStream.toByteArray();

                AccommodationPhoto photo = new AccommodationPhoto(accommodation, photoData);
                photoController.createPhoto(photo, new AccommodationPhotoController.PhotoCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.d("AddAccommodationFragment", "Photo uploaded successfully");
                        if (selectedImagesUris.indexOf(uri) == selectedImagesUris.size() - 1) {
                            clearForm();
                            navigateBackWithSuccess();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("AddAccommodationFragment", "Error uploading photo: " + error);
                    }
                });
            } catch (IOException e) {
                Log.e("AddAccommodationFragment", "Error processing image", e);
            }
        }
    }


    /**
     * Navega de regreso con un mensaje de éxito.
     */
    private void navigateBackWithSuccess() {
        getParentFragmentManager().popBackStack();
        // Notificar al AccommodationsFragment para mostrar el diálogo de éxito
        getParentFragmentManager().setFragmentResult("accommodationResult", new Bundle());
    }

    /**
     * Limpia los campos del formulario.
     */
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
