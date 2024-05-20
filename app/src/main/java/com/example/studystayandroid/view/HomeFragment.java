package com.example.studystayandroid.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Accommodation;
import com.example.studystayandroid.model.User;
import com.example.studystayandroid.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccommodationAdapter adapter;
    private List<Accommodation> accommodationList;

    private static final String URL_ACCOMMODATIONS = "http://" + Constants.IP + "/studystay/getAccommodations.php";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        accommodationList = new ArrayList<>();
        adapter = new AccommodationAdapter(accommodationList);
        recyclerView.setAdapter(adapter);

        fetchAccommodations();
    }

    private void fetchAccommodations() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        Log.d("HomeFragment", "Fetching accommodations...");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL_ACCOMMODATIONS,
                null,
                response -> {
                    Log.d("HomeFragment", "Response received: " + response.toString());
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject accommodationObject = response.getJSONObject(i);

                            Long accommodationId = accommodationObject.getLong("AccommodationId");
                            Long ownerId = accommodationObject.getLong("OwnerId");

                            User user=new User();
                                   /*Hay que hacer el getById y tal*/
                                    user.setUserId(ownerId);






                            String address = accommodationObject.getString("Address");
                            String city = accommodationObject.getString("City");
                            BigDecimal price = new BigDecimal(accommodationObject.getString("Price"));
                            String description = accommodationObject.getString("Description");
                            int capacity = accommodationObject.getInt("Capacity");
                            String services = accommodationObject.getString("Services");
                            boolean availability = accommodationObject.getBoolean("Availability"); // Leer como booleano
                            double rating = accommodationObject.getDouble("Rating");
                            Accommodation accommodation = new Accommodation(user, address, city, price, description, capacity, services);
                            accommodation.setRating(rating);
                            accommodation.setAvailability(availability);
                            accommodationList.add(accommodation);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("HomeFragment", "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("HomeFragment", "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("HomeFragment", "Error code: " + error.networkResponse.statusCode);
                        String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Log.e("HomeFragment", "Error body: " + responseBody);
                    }
                }
        );

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000, // Timeout in milliseconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
    }
}
