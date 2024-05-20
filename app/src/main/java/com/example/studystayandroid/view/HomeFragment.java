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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.studystayandroid.R;
import com.example.studystayandroid.model.Accommodation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AccommodationAdapter adapter;
    private List<Accommodation> accommodationList;

    private static final String URL = "http://10.0.2.2/studystay/get_accommodations.php"; // Cambia esto según tu IP

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
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject accommodation = response.getJSONObject(i);

                                Accommodation a = new Accommodation();
                                a.setAccommodationId(accommodation.getLong("AccommodationId"));
                                // Aquí podrías necesitar hacer una consulta para obtener el usuario propietario
                                // a.setOwner(new User(accommodation.getInt("OwnerId")));
                                a.setAddress(accommodation.getString("Address"));
                                a.setCity(accommodation.getString("City"));
                                a.setPrice(new BigDecimal(accommodation.getString("Price")));
                                a.setDescription(accommodation.getString("Description"));
                                a.setCapacity(accommodation.getInt("Capacity"));
                                a.setServices(accommodation.getString("Services"));
                                a.setAvailability(accommodation.getBoolean("Availability"));
                                a.setRating(accommodation.getDouble("Rating"));
                                accommodationList.add(a);
                            }
                            Log.d("HomeFragment", "Accommodations fetched: " + accommodationList.size());
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("HomeFragment", "JSON parsing error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("HomeFragment", "Volley error: " + error.getMessage());
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }
}
