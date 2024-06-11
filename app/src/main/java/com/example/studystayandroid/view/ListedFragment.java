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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.studystayandroid.controller.ConversationController;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.AccommodationPhoto;
import com.example.studystayandroid.model.Conversation;
import com.example.studystayandroid.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento para mostrar los alojamientos listados por el usuario actual.
 */
public class ListedFragment extends Fragment {

    private RecyclerView recyclerView;
    private SimpleAccommodationAdapter adapter;
    private TextView emptyView;
    private User currentUser;

    /**
     * Crea una nueva instancia de ListedFragment.
     *
     * @param user El usuario actual.
     * @return Una nueva instancia de ListedFragment.
     */
    public static ListedFragment newInstance(User user) {
        ListedFragment fragment = new ListedFragment();
        Bundle args = new Bundle();
        args.putSerializable("currentUser", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewListed);
        emptyView = view.findViewById(R.id.emptyViewListed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SimpleAccommodationAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(this::showAccommodationDetailDialog);

        getListedAccommodations();
    }

    /**
     * Obtiene los alojamientos listados por el usuario actual.
     */
    private void getListedAccommodations() {
        if (getArguments() != null) {
            currentUser = (User) getArguments().getSerializable("currentUser");
        }

        AccommodationController accommodationController = new AccommodationController(getContext());
        accommodationController.getAccommodations(new AccommodationController.AccommodationListCallback() {
            @Override
            public void onSuccess(List<Accommodation> accommodations) {
                List<Accommodation> listedAccommodations = new ArrayList<>();

                for (Accommodation accommodation : accommodations) {
                    if (accommodation.getOwner().getUserId().equals(currentUser.getUserId())) {
                        listedAccommodations.add(accommodation);
                    }
                }

                adapter.setAccommodations(listedAccommodations);
                updateUI(listedAccommodations);
            }

            @Override
            public void onError(String error) {
                Log.e("ListedFragment", "Error fetching accommodations: " + error);
            }
        });
    }

    /**
     * Actualiza la UI según la lista de alojamientos.
     *
     * @param accommodations Lista de alojamientos.
     */
    private void updateUI(List<Accommodation> accommodations) {
        if (accommodations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Muestra el diálogo de detalles del alojamiento.
     *
     * @param accommodation El alojamiento seleccionado.
     */
    private void showAccommodationDetailDialog(Accommodation accommodation) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_listed_accommodation_detail, null);

        Button backButton = dialogView.findViewById(R.id.back_button);
        ViewPager2 imageCarousel = dialogView.findViewById(R.id.imageCarousel);
        TextView addressTextView = dialogView.findViewById(R.id.address);
        TextView descriptionTextView = dialogView.findViewById(R.id.description);
        TextView rating = dialogView.findViewById(R.id.rating);
        TextView availabilityTextView = dialogView.findViewById(R.id.availability);
        TextView priceTextView = dialogView.findViewById(R.id.price);
        TextView ownerTextView = dialogView.findViewById(R.id.ownerName);
        Button mapButton = dialogView.findViewById(R.id.map_button2);
        Button reviewButton = dialogView.findViewById(R.id.reviewButton);

        List<byte[]> photos = new ArrayList<>();
        for (AccommodationPhoto photo : accommodation.getPhotos()) {
            photos.add(photo.getPhotoData());
        }
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(photos, getContext());
        imageCarousel.setAdapter(adapter);

        rating.setText(String.valueOf(accommodation.getRating()));
        availabilityTextView.setText(accommodation.isAvailability() ? "Available" : "Not Available");
        ownerTextView.setText("Owned by " + accommodation.getOwner().getName() + " " + accommodation.getOwner().getLastName());
        addressTextView.setText(accommodation.getAddress() + ", " + accommodation.getCity());
        descriptionTextView.setText(accommodation.getDescription());
        priceTextView.setText(String.format("€%.2f", accommodation.getPrice()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
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

        dialog.show();
    }

    /**
     * Muestra las opciones de contacto con el propietario.
     *
     * @param owner El propietario del alojamiento.
     * @param parentDialog El diálogo padre que debe cerrarse si es necesario.
     */
    private void showContactOptions(User owner, AlertDialog parentDialog) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_contact_options, null);

        Button callButton = dialogView.findViewById(R.id.callButton);
        Button chatButton = dialogView.findViewById(R.id.detailedAccommodationButton);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        callButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + owner.getPhone()));
            startActivity(intent);
            dialog.dismiss();
            parentDialog.dismiss(); // Close the parent dialog as well
        });

        chatButton.setOnClickListener(v -> {
            createConversationAndOpenMessageFragment(owner, parentDialog);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Crea una nueva conversación y abre el fragmento de mensajes.
     *
     * @param owner El propietario del alojamiento.
     * @param parentDialog El diálogo padre que debe cerrarse si es necesario.
     */
    private void createConversationAndOpenMessageFragment(User owner, AlertDialog parentDialog) {
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.getConversations(currentUser.getUserId(), new ConversationController.ConversationListCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                for (Conversation conversation : conversations) {
                    if ((conversation.getUser1Id().equals(currentUser.getUserId()) && conversation.getUser2Id().equals(owner.getUserId())) ||
                            (conversation.getUser1Id().equals(owner.getUserId()) && conversation.getUser2Id().equals(currentUser.getUserId()))) {
                        openMessageFragment(conversation);
                        parentDialog.dismiss(); // Close the parent dialog
                        return;
                    }
                }
                createNewConversation(owner, parentDialog);
            }

            @Override
            public void onError(String error) {
                Log.e("ListedFragment", "Error fetching conversations: " + error);
                createNewConversation(owner, parentDialog);
            }
        });
    }

    /**
     * Crea una nueva conversación.
     *
     * @param owner El propietario del alojamiento.
     * @param parentDialog El diálogo padre que debe cerrarse si es necesario.
     */
    private void createNewConversation(User owner, AlertDialog parentDialog) {
        Conversation newConversation = new Conversation(null, currentUser.getUserId(), owner.getUserId(), new ArrayList<>());
        ConversationController conversationController = new ConversationController(getContext());
        conversationController.createConversation(newConversation, new ConversationController.ConversationCallback() {
            @Override
            public void onSuccess(Conversation createdConversation) {
                openMessageFragment(createdConversation);
                parentDialog.dismiss(); // Close the parent dialog
            }

            @Override
            public void onSuccess(Object result) {
                // Handle any optional logic if needed
            }

            @Override
            public void onError(String error) {
                Log.e("ListedFragment", "Error creating conversation: " + error);
            }
        });
    }

    /**
     * Abre el fragmento de mensajes.
     *
     * @param conversation La conversación a mostrar.
     */
    private void openMessageFragment(Conversation conversation) {
        MessageFragment messageFragment = MessageFragment.newInstance(conversation);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messageFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Muestra la ubicación del alojamiento en el mapa.
     *
     * @param accommodation El alojamiento a mostrar en el mapa.
     */
    private void showAccommodationOnMap(Accommodation accommodation) {
        List<Accommodation> singleAccommodationList = new ArrayList<>();
        singleAccommodationList.add(accommodation);
        MapDialogFragment mapDialogFragment = new MapDialogFragment(singleAccommodationList);
        mapDialogFragment.show(getParentFragmentManager(), "MapDialogFragment");
    }
}
