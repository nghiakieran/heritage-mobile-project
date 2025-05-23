package hcmute.edu.vn.heritageproject.views.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.views.adapters.EventAdapter;
import hcmute.edu.vn.heritageproject.models.CulturalEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private View view;
    private AutoCompleteTextView searchEditText;
    private RecyclerView recyclerViewEvents;
    private ImageButton fullScreenButton;
    private boolean isFullScreen = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private PlacesClient placesClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize Places API
        Places.initialize(requireContext(), "AIzaSyARrkvpVCm0ugSp_amklEgiPqViReNVVj4"); // Replace with your Google Maps API key
        placesClient = Places.createClient(requireContext());

        // Initialize views
        mapView = view.findViewById(R.id.mapView);
        searchEditText = view.findViewById(R.id.searchEditText);
        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
        fullScreenButton = view.findViewById(R.id.fullScreenButton);

        // Setup RecyclerView with horizontal LinearLayoutManager
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewEvents.setVisibility(View.GONE);

        // Initialize MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Setup search autocomplete
        setupSearchAutocomplete();

        // Setup full-screen toggle
        fullScreenButton.setOnClickListener(v -> toggleFullScreen());

        // Request location permissions
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Set default location (Hanoi)
        LatLng defaultLocation = new LatLng(21.0285, 105.8542);
        googleMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f));

        // Hide empty message
        View emptyMapMessage = view.findViewById(R.id.emptyMapMessage);
        if (emptyMapMessage != null) {
            emptyMapMessage.setVisibility(View.GONE);
        }

        // Enable My Location if permission granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Handle map click to place marker and log coordinates
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            Log.d("MapFragment", "Latitude: " + latLng.latitude + ", Longitude: " + latLng.longitude);
            showEventList();
        });
    }

    private void setupSearchAutocomplete() {
        // Use AutoCompleteTextView for dropdown suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
        searchEditText.setAdapter(adapter);
        searchEditText.setThreshold(1); // Show suggestions after 1 character

        // Ensure dropdown is wide enough
        searchEditText.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        searchEditText.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPlace = (String) parent.getItemAtPosition(position);
            Log.d("MapFragment", "Selected suggestion: " + selectedPlace);
            // Fetch place details
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setTypesFilter(Arrays.asList("geocode", "establishment"))
                    .setQuery(selectedPlace)
                    .build();
            placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
                if (!response.getAutocompletePredictions().isEmpty()) {
                    com.google.android.libraries.places.api.model.AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                    placesClient.fetchPlace(com.google.android.libraries.places.api.net.FetchPlaceRequest.builder(prediction.getPlaceId(), Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME)).build())
                            .addOnSuccessListener(fetchPlaceResponse -> {
                                Place place = fetchPlaceResponse.getPlace();
                                LatLng latLng = place.getLatLng();
                                if (latLng != null) {
                                    googleMap.clear();
                                    googleMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
                                    Log.d("MapFragment", "Selected Place: " + place.getName() + ", Latitude: " + latLng.latitude + ", Longitude: " + latLng.longitude);
                                    showEventList();
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("MapFragment", "Failed to fetch place: " + e.getMessage());
                            });
                } else {
                    Log.w("MapFragment", "No predictions found for: " + selectedPlace);
                }
            }).addOnFailureListener(e -> {
                Log.e("MapFragment", "Failed to fetch predictions: " + e.getMessage());
            });
        });

        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    Log.d("MapFragment", "Fetching suggestions for query: " + s);
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setTypesFilter(Arrays.asList("geocode", "establishment"))
                            .setQuery(s.toString())
                            .build();
                    placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
                        List<String> suggestions = new ArrayList<>();
                        for (com.google.android.libraries.places.api.model.AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            suggestions.add(prediction.getPrimaryText(null).toString());
                        }
                        Log.d("MapFragment", "Suggestions received: " + suggestions);
                        adapter.clear();
                        adapter.addAll(suggestions);
                        adapter.notifyDataSetChanged();
                        searchEditText.showDropDown(); // Force dropdown to show
                    }).addOnFailureListener(e -> {
                        Log.e("MapFragment", "Failed to fetch suggestions: " + e.getMessage());
                    });
                } else {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void toggleFullScreen() {
        isFullScreen = !isFullScreen;
        if (isFullScreen) {
            recyclerViewEvents.setVisibility(View.GONE);
            view.findViewById(R.id.searchBarCard).setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
            );
            mapView.setLayoutParams(params);
            fullScreenButton.setImageResource(R.drawable.ic_exit_fullscreen);
        } else {
            recyclerViewEvents.setVisibility(View.VISIBLE);
            view.findViewById(R.id.searchBarCard).setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            );
            params.topToBottom = R.id.searchBarCard;
            params.bottomToTop = R.id.recyclerViewEvents;
            params.matchConstraintPercentHeight = 0.5f;
            mapView.setLayoutParams(params);
            fullScreenButton.setImageResource(R.drawable.ic_exit_fullscreen);
        }
    }

    private void showEventList() {
        List<CulturalEvent> events = new ArrayList<>();
        events.add(new CulturalEvent("Lễ hội Đền Hùng",
                "03/06/2025 - 10/06/2025",
                "Phú Thọ",
                "Lễ hội tưởng nhớ các Vua Hùng đã có công dựng nước",
                R.drawable.event_den_hung));
        events.add(new CulturalEvent("Festival Huế 2025",
                "01/07/2025 - 06/07/2025",
                "Thừa Thiên Huế",
                "Lễ hội văn hóa, nghệ thuật quốc tế diễn ra 2 năm một lần",
                R.drawable.event_hue));
        events.add(new CulturalEvent("Hội Đèn Lồng Hội An",
                "14/08/2025",
                "Quảng Nam",
                "Đêm phố cổ lung linh ánh đèn lồng vào đêm rằm",
                R.drawable.event_hoian));

        EventAdapter eventAdapter = new EventAdapter(events);
        recyclerViewEvents.setAdapter(eventAdapter);
        recyclerViewEvents.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
        }
    }
}