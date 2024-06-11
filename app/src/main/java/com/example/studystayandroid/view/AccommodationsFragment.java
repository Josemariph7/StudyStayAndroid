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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.studystayandroid.R;
import com.example.studystayandroid.controller.AccommodationController;
import com.example.studystayandroid.controller.AccommodationPhotoController;
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.controller.UserController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;
import com.example.studystayandroid.utils.Constants;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragmento para mostrar y gestionar alojamientos.
 */
public class AccommodationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccommodationAdapter adapter;
    private List<Accommodation> accommodationList;
    private AccommodationController accommodationController;
    private UserController userController;
    private User currentUser;

    private Spinner filterCitySpinner;
    private Spinner filterCapacitySpinner;
    private CheckBox filterAvailability;
    private Button filterButton;
    private Button mapButton;
    private Button addAccommodationButton;
    private Button reviewButton;
    private Button applyFiltersButton;
    private LinearLayout filtersContainer;

    private static final String URL_ACCOMMODATIONS = "http://" + Constants.IP + "/studystay/getAccommodations.php";

    /**
     * Constructor público y vacío requerido.
     */
    public AccommodationsFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().setTitle("StudyStay - Accommodations");
        }
        getParentFragmentManager().setFragmentResultListener("accommodationResult", this, (requestKey, result) -> {
            showSuccessDialog("Accommodation posted successfully.");
        });

        return inflater.inflate(R.layout.fragment_accommodations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("currentUser");
            Log.d("AccommodationsFragment", "User: " + currentUser.toString());
        }

        accommodationController = new AccommodationController(requireContext());
        userController = new UserController(requireContext());

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        accommodationList = new ArrayList<>();
        adapter = new AccommodationAdapter(accommodationList, getContext());
        recyclerView.setAdapter(adapter);

        filterCitySpinner = view.findViewById(R.id.filter_city_spinner);
        filterCapacitySpinner = view.findViewById(R.id.filter_capacity_spinner);
        filterAvailability = view.findViewById(R.id.filter_availability);
        filterButton = view.findViewById(R.id.filter_button);
        mapButton = view.findViewById(R.id.map_button);
        addAccommodationButton = view.findViewById(R.id.add_accommodation_button);
        applyFiltersButton = view.findViewById(R.id.apply_filters_button);
        filtersContainer = view.findViewById(R.id.filters_container);

        filterButton.setOnClickListener(v -> toggleFiltersVisibility());
        mapButton.setOnClickListener(v -> openMap());
        addAccommodationButton.setOnClickListener(v -> openAddAccommodationFragment(currentUser));
        applyFiltersButton.setOnClickListener(v -> applyFilters());

        setupSpinners();
        fetchAccommodations();

        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.cities_array,
                R.layout.spinner_item);

        cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        filterCitySpinner.setAdapter(cityAdapter);

        ArrayAdapter<CharSequence> capacityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.capacity_array,
                R.layout.spinner_item);

        capacityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        filterCapacitySpinner.setAdapter(capacityAdapter);

        adapter.setOnItemClickListener(this::showAccommodationDetailDialog);
        adapter.setOnItemLongClickListener(this::showAccommodationOptionsDialog);
    }

    /**
     * Alterna la visibilidad de las opciones de filtro.
     */
    private void toggleFiltersVisibility() {
        if (filtersContainer.getVisibility() == View.VISIBLE) {
            filtersContainer.setVisibility(View.GONE);
        } else {
            filtersContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configura los spinners para los filtros de ciudad y capacidad.
     */
    private void setupSpinners() {
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.cities_array,
                android.R.layout.simple_spinner_item);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCitySpinner.setAdapter(cityAdapter);

        ArrayAdapter<CharSequence> capacityAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.capacity_array,
                android.R.layout.simple_spinner_item);
        capacityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCapacitySpinner.setAdapter(capacityAdapter);
    }

    /**
     * Obtiene la lista de alojamientos.
     */
    private void fetchAccommodations() {
        accommodationController.getAccommodations(new AccommodationController.AccommodationListCallback() {
            @Override
            public void onSuccess(List<Accommodation> accommodations) {
                accommodationList.clear();
                accommodationList.addAll(accommodations);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error fetching accommodations: " + error);
            }
        });
    }

    /**
     * Muestra un diálogo de éxito con un mensaje dado.
     *
     * @param message El mensaje a mostrar.
     */
    private void showSuccessDialog(String message) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_success, null);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Success");
        dialogMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        buttonConfirm.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Aplica los filtros seleccionados a la lista de alojamientos.
     */
    private void applyFilters() {
        String selectedCity = filterCitySpinner.getSelectedItem().toString();
        String selectedCapacityStr = filterCapacitySpinner.getSelectedItem().toString();
        boolean isAvailable = filterAvailability.isChecked();

        Integer selectedCapacity = null;
        if (!selectedCapacityStr.equals("All")) {
            try {
                selectedCapacity = Integer.parseInt(selectedCapacityStr);
            } catch (NumberFormatException e) {
                selectedCapacity = null;
            }
        }

        Integer finalSelectedCapacity = selectedCapacity;
        accommodationController.getAccommodations(new AccommodationController.AccommodationListCallback() {
            @Override
            public void onSuccess(List<Accommodation> accommodations) {
                List<Accommodation> filteredList = new ArrayList<>();
                for (Accommodation accommodation : accommodations) {
                    boolean matchesCity = selectedCity.equals("All") || accommodation.getCity().equals(selectedCity);
                    boolean matchesCapacity = finalSelectedCapacity == null || accommodation.getCapacity() == finalSelectedCapacity;
                    boolean matchesAvailability = accommodation.isAvailability() == isAvailable;

                    if (matchesCity && matchesCapacity && matchesAvailability) {
                        filteredList.add(accommodation);
                    }
                }
                accommodationList.clear();
                accommodationList.addAll(filteredList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error applying filters: " + error);
            }
        });
        toggleFiltersVisibility();
    }

    /**
     * Abre el mapa para mostrar los alojamientos.
     */
    private void openMap() {
        MapDialogFragment mapDialogFragment = new MapDialogFragment(accommodationList);
        mapDialogFragment.show(getParentFragmentManager(), "MapDialogFragment");
    }

    /**
     * Abre el fragmento para agregar un nuevo alojamiento.
     *
     * @param currentUser El usuario actual.
     */
    private void openAddAccommodationFragment(User currentUser) {
        AddAccommodationFragment fragment = new AddAccommodationFragment();

        // Crear un Bundle para pasar el User al fragmento
        Bundle args = new Bundle();
        args.putSerializable("currentUser", currentUser);
        fragment.setArguments(args);

        // Reemplazar el fragmento
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Muestra el diálogo de detalles del alojamiento.
     *
     * @param accommodation El alojamiento para mostrar los detalles.
     */
    private void showAccommodationDetailDialog(Accommodation accommodation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_accommodation_detail, null);

        Button backButton = dialogView.findViewById(R.id.back_button);
        ViewPager2 imageCarousel = dialogView.findViewById(R.id.imageCarousel);
        TextView addressTextView = dialogView.findViewById(R.id.address);
        TextView descriptionTextView = dialogView.findViewById(R.id.description);
        TextView rating = dialogView.findViewById(R.id.rating);
        TextView availabilityTextView = dialogView.findViewById(R.id.availability);
        TextView priceTextView = dialogView.findViewById(R.id.price);
        TextView ownerTextView = dialogView.findViewById(R.id.ownerName);
        Button bookButton = dialogView.findViewById(R.id.bookButton);
        Button contactButton = dialogView.findViewById(R.id.contactButton);
        Button mapButton = dialogView.findViewById(R.id.map_button2);
        reviewButton = dialogView.findViewById(R.id.reviewButton);

        // Obtener las fotos del alojamiento usando AccommodationPhotoController
        AccommodationPhotoController controller = new AccommodationPhotoController(requireContext());
        controller.getPhotos(new AccommodationPhotoController.PhotoListCallback() {
            @Override
            public void onSuccess(List<AccommodationPhoto> photos) {
                List<byte[]> photoDataList = new ArrayList<>();
                for (AccommodationPhoto photo : photos) {
                    if (photo.getAccommodation().getAccommodationId().equals(accommodation.getAccommodationId())) {
                        photoDataList.add(Base64.decode(photo.getPhotoData(), Base64.DEFAULT));
                    }
                }

                if (photoDataList.isEmpty()) {
                    Log.d("AccommodationsFragment", "No photos found for accommodation ID: " + accommodation.getAccommodationId());
                    photoDataList.add(getDefaultImageBytes());
                } else {
                    Log.d("AccommodationsFragment", "Photos found for accommodation ID: " + accommodation.getAccommodationId());
                }

                // Crear y establecer el adaptador de carrusel de imágenes
                ImageCarouselAdapter adapter = new ImageCarouselAdapter(photoDataList, getContext());
                imageCarousel.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error loading photos: " + error);
                List<byte[]> defaultImageList = Collections.singletonList(getDefaultImageBytes());
                imageCarousel.setAdapter(new ImageCarouselAdapter(defaultImageList, getContext()));
            }
        });

        rating.setText(String.valueOf(accommodation.getRating()));
        availabilityTextView.setText(accommodation.isAvailability() ? "Available" : "Not Available");
        ownerTextView.setText("Owned by " + accommodation.getOwner().getName() + " " + accommodation.getOwner().getLastName());
        addressTextView.setText(accommodation.getAddress() + ", " + accommodation.getCity());
        descriptionTextView.setText(accommodation.getDescription());
        priceTextView.setText(String.format("€%.2f", accommodation.getPrice()));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        backButton.setOnClickListener(v -> dialog.dismiss());

        mapButton.setOnClickListener(v -> showAccommodationOnMap(accommodation));

        reviewButton.setOnClickListener(v -> {
            ReviewFragment reviewFragment = new ReviewFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("accommodation", accommodation);
            bundle.putSerializable("currentUser", currentUser);
            reviewFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, reviewFragment)
                    .addToBackStack(null)
                    .commit();
            dialog.dismiss();
        });

        bookButton.setOnClickListener(v -> {
            BookingFragment bookingFragment = new BookingFragment();
            Bundle args = new Bundle();
            args.putSerializable("accommodation", accommodation);
            args.putSerializable("currentUser", currentUser);
            bookingFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, bookingFragment)
                    .addToBackStack(null)
                    .commit();
            dialog.dismiss();
        });

        contactButton.setOnClickListener(v -> {
            showContactOptions(accommodation.getOwner(), dialog);
        });

        dialog.show();
    }

    /**
     * Obtiene los bytes de la imagen por defecto desde los recursos.
     *
     * @return Array de bytes de la imagen por defecto.
     */
    private byte[] getDefaultImageBytes() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.accommodation);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Muestra el diálogo de opciones del alojamiento.
     *
     * @param accommodation El alojamiento para mostrar opciones.
     */
    private void showAccommodationOptionsDialog(Accommodation accommodation) {
        if (accommodation.getOwner().getUserId().equals(currentUser.getUserId())) {
            showDeleteConfirmationDialog(accommodation);
        } else {
            showOptionsDialog(accommodation);
        }
    }

    /**
     * Muestra un diálogo con opciones para el alojamiento seleccionado.
     *
     * @param accommodation El alojamiento seleccionado.
     */
    private void showOptionsDialog(Accommodation accommodation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_accommodation_options, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        Button viewProfileButton = dialogView.findViewById(R.id.viewProfileButton);
        Button viewMapButton = dialogView.findViewById(R.id.viewMapButton);

        dialogTitle.setText("Choose an option");

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        viewProfileButton.setOnClickListener(v -> {
            openOwnerProfile(accommodation.getOwner());
            dialog.dismiss();
        });

        viewMapButton.setOnClickListener(v -> {
            showAccommodationOnMap(accommodation);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Muestra un diálogo de confirmación de eliminación para el alojamiento seleccionado.
     *
     * @param accommodation El alojamiento seleccionado.
     */
    private void showDeleteConfirmationDialog(Accommodation accommodation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        dialogTitle.setText("Confirm Delete");
        dialogMessage.setText("Are you sure you want to delete this accommodation?");

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonConfirm.setOnClickListener(v -> {
            deleteAccommodation(accommodation);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Elimina el alojamiento seleccionado.
     *
     * @param accommodation El alojamiento a eliminar.
     */
    private void deleteAccommodation(Accommodation accommodation) {
        accommodationController.deleteAccommodation(accommodation.getAccommodationId(), new AccommodationController.AccommodationCallback() {
            @Override
            public void onSuccess(Object result) {
                fetchAccommodations(); // Refrescar la lista después de la eliminación
            }

            @Override
            public void onSuccess(Accommodation accommodation) {
                fetchAccommodations();
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error deleting accommodation: " + error);
            }
        });
    }

    /**
     * Abre el perfil del propietario del alojamiento.
     *
     * @param owner El propietario del alojamiento.
     */
    private void openOwnerProfile(User owner) {
        StrangeProfileFragment fragment = StrangeProfileFragment.newInstance(owner);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Muestra el alojamiento seleccionado en un mapa.
     *
     * @param accommodation El alojamiento seleccionado.
     */
    private void showAccommodationOnMap(Accommodation accommodation) {
        List<Accommodation> singleAccommodationList = new ArrayList<>();
        singleAccommodationList.add(accommodation);
        MapDialogFragment mapDialogFragment = new MapDialogFragment(singleAccommodationList);
        mapDialogFragment.show(getParentFragmentManager(), "MapDialogFragment");
    }

    /**
     * Muestra opciones de contacto para el propietario del alojamiento.
     *
     * @param owner        El propietario del alojamiento.
     * @param parentDialog El diálogo padre.
     */
    private void showContactOptions(User owner, androidx.appcompat.app.AlertDialog parentDialog) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_contact_options, null);

        Button callButton = dialogView.findViewById(R.id.callButton);
        Button chatButton = dialogView.findViewById(R.id.detailedAccommodationButton);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        callButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + owner.getPhone()));
            startActivity(intent);
            dialog.dismiss();
            parentDialog.dismiss(); // Cerrar también el diálogo padre
        });

        chatButton.setOnClickListener(v -> {
            createConversationAndOpenMessageFragment(owner, parentDialog);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Crea una conversación y abre el fragmento de mensajes.
     *
     * @param owner        El propietario del alojamiento.
     * @param parentDialog El diálogo padre.
     */
    private void createConversationAndOpenMessageFragment(User owner, androidx.appcompat.app.AlertDialog parentDialog) {
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.getConversations(currentUser.getUserId(), new ConversationController.ConversationListCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                for (Conversation conversation : conversations) {
                    if ((conversation.getUser1Id().equals(currentUser.getUserId()) && conversation.getUser2Id().equals(owner.getUserId())) ||
                            (conversation.getUser1Id().equals(owner.getUserId()) && conversation.getUser2Id().equals(currentUser.getUserId()))) {
                        openMessageFragment(conversation);
                        parentDialog.dismiss(); // Cerrar el diálogo padre
                        return;
                    }
                }
                createNewConversation(owner, parentDialog);
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error fetching conversations: " + error);
                createNewConversation(owner, parentDialog);
            }
        });
    }

    /**
     * Crea una nueva conversación con el propietario del alojamiento.
     *
     * @param owner        El propietario del alojamiento.
     * @param parentDialog El diálogo padre.
     */
    private void createNewConversation(User owner, androidx.appcompat.app.AlertDialog parentDialog) {
        Conversation newConversation = new Conversation(null, currentUser.getUserId(), owner.getUserId(), new ArrayList<>());
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.createConversation(newConversation, new ConversationController.ConversationCallback() {
            @Override
            public void onSuccess(Conversation createdConversation) {
                openMessageFragment(createdConversation);
                parentDialog.dismiss(); // Cerrar el diálogo padre
            }

            @Override
            public void onSuccess(Object result) {
                // Manejar cualquier lógica opcional si es necesario
            }

            @Override
            public void onError(String error) {
                Log.e("AccommodationsFragment", "Error creating conversation: " + error);
            }
        });
    }

    /**
     * Abre el fragmento de mensajes para la conversación especificada.
     *
     * @param conversation La conversación a abrir.
     */
    private void openMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }
}
