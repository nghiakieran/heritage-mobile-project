package hcmute.edu.vn.heritageproject.views.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;
import hcmute.edu.vn.heritageproject.api.HeritageApiService;
import hcmute.edu.vn.heritageproject.views.adapters.HeritageAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapFragment extends Fragment {

    private MapView mapView;
    private View rootView;
    private ImageButton fullScreenButton;
    private boolean isFullScreen = false;
    private AutoCompleteTextView searchEditText;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final long SEARCH_DELAY_MS = 500; // 0.5 giây delay
    private Runnable searchRunnable;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private RecyclerView recyclerViewNearbyHeritages;
    private HeritageAdapter nearbyHeritagesAdapter;
    private List<Heritage> nearbyHeritages = new ArrayList<>();
    private HeritageApiService heritageApiService;
    private TextView messageText;
    private FloatingActionButton myLocationButton;
    private static final int DEFAULT_NEARBY_LIMIT = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Khởi tạo OSMDroid
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(requireActivity().getPackageName());

        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        initializeViews();
        initializeMap();
        setupFullScreenButton();
        setupSearchBar();
        requestLocationPermissions();

        return rootView;
    }    private void initializeViews() {
        // Khởi tạo HeritageApiService
        heritageApiService = HeritageApiService.getInstance();
        
        mapView = rootView.findViewById(R.id.mapView);
        fullScreenButton = rootView.findViewById(R.id.fullScreenButton);
        searchEditText = rootView.findViewById(R.id.searchEditText);
        messageText = rootView.findViewById(R.id.messageText);
        myLocationButton = rootView.findViewById(R.id.myLocationButton);        // Initialize RecyclerView for nearby heritages
        recyclerViewNearbyHeritages = rootView.findViewById(R.id.recyclerViewNearbyHeritages);
        recyclerViewNearbyHeritages.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        nearbyHeritagesAdapter = new HeritageAdapter(nearbyHeritages);
        recyclerViewNearbyHeritages.setAdapter(nearbyHeritagesAdapter);

        // Set click listener for heritage items
        nearbyHeritagesAdapter.setOnHeritageClickListener(heritage -> {
            // Di chuyển map đến vị trí di tích được chọn
            if (heritage.getLatitude() != null && heritage.getLongitude() != null) {
                GeoPoint heritagePoint = new GeoPoint(heritage.getLatitude(), heritage.getLongitude());
                mapView.getController().animateTo(heritagePoint);
                mapView.getController().setZoom(16.0);
            }
        });
    }    private void initializeMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(12.0);
        
        // Tắt các nút zoom mặc định
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        
        // Bật đa chạm để zoom
        mapView.setMultiTouchControls(true);
        
        // Set vị trí mặc định (Ho Chi Minh City)
        GeoPoint startPoint = new GeoPoint(10.8231, 106.6297);
        mapView.getController().setCenter(startPoint);
        
        // Thêm marker mặc định và tải di tích gần đó
        addMarker(startPoint, "Hồ Chí Minh");
        loadNearbyHeritages(10.8231, 106.6297);

        // Thêm tap listener
        Overlay touchOverlay = new Overlay(requireContext()) {
            @Override
            public boolean onSingleTapConfirmed(android.view.MotionEvent e, MapView mapView) {
                android.graphics.Point screenPoint = new android.graphics.Point();
                screenPoint.x = (int) e.getX();
                screenPoint.y = (int) e.getY();
                  GeoPoint geoPoint = (GeoPoint) mapView.getProjection().fromPixels(screenPoint.x, screenPoint.y);
                
                // Xóa tất cả markers
                clearAllMarkers();

                // Thêm marker cho vị trí được chọn
                addMarker(geoPoint, "Vị trí đã chọn");

                // Tìm các di tích gần điểm được chọn
                heritageApiService.getNearestHeritages(
                    geoPoint.getLatitude(),
                    geoPoint.getLongitude(),
                    DEFAULT_NEARBY_LIMIT,
                    new HeritageApiService.ApiCallback<HeritageResponse>() {
                        @Override
                        public void onSuccess(HeritageResponse result) {
                            requireActivity().runOnUiThread(() -> {
                                if (result != null && result.getHeritages() != null) {
                                    nearbyHeritages.clear();
                                    nearbyHeritages.addAll(result.getHeritages());
                                    nearbyHeritagesAdapter.notifyDataSetChanged();
                                    
                                    // Thêm marker cho mỗi di tích tìm thấy
                                    for (Heritage heritage : result.getHeritages()) {
                                        if (heritage.getLatitude() != null && heritage.getLongitude() != null) {
                                            GeoPoint heritagePoint = new GeoPoint(
                                                heritage.getLatitude(),
                                                heritage.getLongitude()
                                            );
                                            addMarker(heritagePoint, heritage.getName());
                                        }
                                    }
                                    
                                    showMessage(String.format("Tìm thấy %d di tích gần đây", result.getHeritages().size()));
                                } else {
                                    showMessage("Không tìm thấy di tích nào gần đây");
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            requireActivity().runOnUiThread(() -> 
                                showMessage("Lỗi khi tìm di tích: " + e.getMessage())
                            );
                        }
                    });

                return true;
            }
        };
        mapView.getOverlays().add(touchOverlay);

        // Thêm xử lý cho nút định vị
        myLocationButton.setOnClickListener(v -> {
            requestLocationAndUpdateMap();
        });
    }

    private void setupSearchBar() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (s.length() > 0) {
                        performNominatimSearch(s.toString());
                    }
                };
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchEditText.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPlace = (String) parent.getItemAtPosition(position);
            findPlaceAndMoveCameraThere(selectedPlace);
        });
    }

    private void performNominatimSearch(String query) {
        String country = "vn"; // Tìm kiếm trong Việt Nam
        HttpUrl url = HttpUrl.parse("https://nominatim.openstreetmap.org/search")
                .newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .addQueryParameter("countrycodes", country)
                .addQueryParameter("limit", "5")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", requireActivity().getPackageName())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), "Lỗi tìm kiếm: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        JSONArray results = new JSONArray(jsonData);
                        List<String> suggestions = new ArrayList<>();
                        
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            suggestions.add(place.getString("display_name"));
                        }

                        requireActivity().runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                suggestions
                            );
                            searchEditText.setAdapter(adapter);
                            if (!suggestions.isEmpty()) {
                                searchEditText.showDropDown();
                            }
                        });

                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() -> 
                            Toast.makeText(requireContext(), "Lỗi xử lý dữ liệu: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void findPlaceAndMoveCameraThere(String placeDescription) {
        HttpUrl url = HttpUrl.parse("https://nominatim.openstreetmap.org/search")
                .newBuilder()
                .addQueryParameter("q", placeDescription)
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "1")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", requireActivity().getPackageName())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), "Lỗi tìm kiếm: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        JSONArray results = new JSONArray(jsonData);
                        
                        if (results.length() > 0) {
                            JSONObject place = results.getJSONObject(0);
                            double lat = place.getDouble("lat");
                            double lon = place.getDouble("lon");
                            String name = place.getString("display_name");

                            requireActivity().runOnUiThread(() -> {
                                GeoPoint point = new GeoPoint(lat, lon);
                                mapView.getController().animateTo(point);
                                mapView.getController().setZoom(16.0);
                                
                                // Xóa markers cũ
                                clearAllMarkers();

                                // Thêm marker mới
                                addMarker(point, name);
                            });
                        }

                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() -> 
                            Toast.makeText(requireContext(), "Lỗi xử lý dữ liệu: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void addMarker(GeoPoint position, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    private void setupFullScreenButton() {
        fullScreenButton.setOnClickListener(v -> toggleFullScreen());
    }

    private void toggleFullScreen() {
        isFullScreen = !isFullScreen;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mapView.getLayoutParams();

        if (isFullScreen) {
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT;
            fullScreenButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            params.height = 0;
            params.matchConstraintPercentHeight = 0.5f;
            fullScreenButton.setImageResource(android.R.drawable.ic_menu_zoom);
        }
        mapView.setLayoutParams(params);
    }

    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void requestLocationAndUpdateMap() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Sử dụng FusedLocationProviderClient để lấy vị trí hiện tại
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            updateMapLocation(location.getLatitude(), location.getLongitude());
                            loadNearbyHeritages(location.getLatitude(), location.getLongitude());
                        } else {
                            showMessage("Không thể lấy vị trí hiện tại");
                        }
                    })
                    .addOnFailureListener(e -> showMessage("Lỗi: " + e.getMessage()));
        } else {
            requestLocationPermissions();
        }
    }

    private void updateMapLocation(double latitude, double longitude) {
        GeoPoint currentLocation = new GeoPoint(latitude, longitude);
        mapView.getController().animateTo(currentLocation);
        mapView.getController().setZoom(14.0);
        
        // Thêm marker cho vị trí hiện tại
        Marker marker = new Marker(mapView);
        marker.setPosition(currentLocation);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Vị trí của bạn");
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }    private void loadNearbyHeritages(double latitude, double longitude) {
        if (heritageApiService == null) {
            showMessage("Lỗi: Không thể kết nối tới dịch vụ");
            return;
        }

        heritageApiService.getNearestHeritages(
            latitude,
            longitude,
            DEFAULT_NEARBY_LIMIT,
            new HeritageApiService.ApiCallback<HeritageResponse>() {
                @Override
                public void onSuccess(HeritageResponse result) {
                    requireActivity().runOnUiThread(() -> {
                        if (result != null && result.getHeritages() != null) {
                            nearbyHeritages.clear();
                            nearbyHeritages.addAll(result.getHeritages());
                            nearbyHeritagesAdapter.notifyDataSetChanged();
                            
                            // Add markers for each heritage site
                            for (Heritage heritage : result.getHeritages()) {
                                if (heritage.getLatitude() != null && heritage.getLongitude() != null) {
                                    GeoPoint heritagePoint = new GeoPoint(
                                        heritage.getLatitude(),
                                        heritage.getLongitude()
                                    );
                                    addMarker(heritagePoint, heritage.getName());
                                }
                            }
                            
                            showMessage(String.format("Tìm thấy %d di tích gần đây", result.getHeritages().size()));
                        } else {
                            showMessage("Không tìm thấy di tích nào gần đây");
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    requireActivity().runOnUiThread(() -> 
                        showMessage("Lỗi khi tìm di tích: " + e.getMessage())
                    );
                }
            });
    }

    private void showMessage(String message) {
        if (messageText != null) {
            messageText.setText(message);
            messageText.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (messageText != null) {
                    messageText.setVisibility(View.GONE);
                }
            }, 3000); // Hide message after 3 seconds
        }
    }

    private void clearAllMarkers() {
        List<Overlay> overlays = mapView.getOverlays();
        overlays.removeIf(overlay -> overlay instanceof Marker);
        mapView.invalidate();
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
        mapView.onDetach();
    }
}