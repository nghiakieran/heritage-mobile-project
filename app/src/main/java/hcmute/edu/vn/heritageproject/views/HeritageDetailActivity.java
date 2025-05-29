package hcmute.edu.vn.heritageproject.views;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.viewpager2.widget.ViewPager2;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.api.HeritageApiService;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;
import hcmute.edu.vn.heritageproject.utils.NetworkUtils;
import hcmute.edu.vn.heritageproject.services.FavoritesService;
import hcmute.edu.vn.heritageproject.models.Favorites;
import hcmute.edu.vn.heritageproject.views.adapters.ImageSliderAdapter;

public class HeritageDetailActivity extends AppCompatActivity {

    private static final String TAG = "HeritageDetailActivity";
    private TextView heritageName;
    private TextView heritageLocation;
    private TextView heritageDescription;
    private TextView heritageRating;
    private TextView heritageFavorites;
    private ImageButton buttonFavorite;
    private FavoritesService favoritesService;
    private boolean isFavorite = false;
    private HeritageApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heritage_detail);

        // Initialize views
        heritageName = findViewById(R.id.heritageName);
        heritageLocation = findViewById(R.id.heritageLocation);
        heritageDescription = findViewById(R.id.heritageDescription);
        heritageRating = findViewById(R.id.heritageRating);
        heritageFavorites = findViewById(R.id.heritageFavorites);
        buttonFavorite = findViewById(R.id.buttonFavorite);
        favoritesService = FavoritesService.getInstance();

        // Setup favorite button
        buttonFavorite.setOnClickListener(v -> toggleFavorite());

        // Initialize API service
        apiService = HeritageApiService.getInstance();

        // Load heritage details
        String heritageId = getIntent().getStringExtra("heritageId");
        if (heritageId != null) {
            loadHeritageDetails(heritageId);
            checkFavoriteStatus(heritageId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin di tích", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadHeritageDetails(String heritageId) {
        apiService.getHeritageById(heritageId, new HeritageApiService.ApiCallback<HeritageResponse>() {
            @Override
            public void onSuccess(HeritageResponse result) {
                runOnUiThread(() -> {
                    try {
                        if (result.getHeritages() != null && !result.getHeritages().isEmpty()) {
                            Heritage heritage = result.getHeritages().get(0);
                            if (heritage.getId() != null && !heritage.getId().isEmpty()) {
                                Log.d(TAG, "Successfully loaded heritage with ID: " + heritage.getId());
                                displayHeritageDetails(heritage);
                            } else {
                                Log.e(TAG, "Loaded heritage has empty ID");
                                Toast.makeText(HeritageDetailActivity.this,
                                        "Chi tiết di tích không hợp lệ: Thiếu ID",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Log.e(TAG, "Heritage list is empty or null");
                            Toast.makeText(HeritageDetailActivity.this,
                                    "Không thể tải chi tiết di tích: Dữ liệu không đúng định dạng",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing heritage data", e);
                        Toast.makeText(HeritageDetailActivity.this,
                                "Lỗi xử lý dữ liệu di tích: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to load heritage details", e);
                    Toast.makeText(HeritageDetailActivity.this,
                            "Lỗi khi tải chi tiết di tích: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayHeritageDetails(Heritage heritage) {
        // Slider ảnh
        ViewPager2 imageSlider = findViewById(R.id.imageSlider);
        LinearLayout sliderIndicator = findViewById(R.id.sliderIndicator);
        if (heritage.getImages() != null && !heritage.getImages().isEmpty()) {
            ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(heritage.getImages());
            imageSlider.setAdapter(sliderAdapter);
            // Indicator (nếu muốn đẹp hơn thì dùng thư viện CircleIndicator3)
            sliderIndicator.removeAllViews();
            for (int i = 0; i < heritage.getImages().size(); i++) {
                View dot = new View(this);
                dot.setBackgroundResource(R.drawable.indicator_dot_unselected);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
                params.setMargins(4, 0, 4, 0);
                dot.setLayoutParams(params);
                sliderIndicator.addView(dot);
            }
            if (sliderIndicator.getChildCount() > 0)
                sliderIndicator.getChildAt(0).setBackgroundResource(R.drawable.indicator_dot_selected);
            imageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    for (int i = 0; i < sliderIndicator.getChildCount(); i++) {
                        sliderIndicator.getChildAt(i).setBackgroundResource(
                            i == position ? R.drawable.indicator_dot_selected : R.drawable.indicator_dot_unselected
                        );
                    }
                }
            });
        } else {
            imageSlider.setVisibility(View.GONE);
            sliderIndicator.setVisibility(View.GONE);
        }

        // Tên, vị trí, mô tả
        ((TextView) findViewById(R.id.heritageName)).setText(heritage.getName());
        ((TextView) findViewById(R.id.heritageLocation)).setText(heritage.getLocation());
        ((TextView) findViewById(R.id.heritageDescription)).setText(heritage.getDescription());

        // Tag
        LinearLayout tagContainer = findViewById(R.id.tagContainer);
        tagContainer.removeAllViews();
        if (heritage.getPopularTags() != null && !heritage.getPopularTags().isEmpty()) {
            for (String tag : heritage.getPopularTags()) {
                TextView tagView = new TextView(this);
                tagView.setText(tag);
                tagView.setBackgroundResource(R.drawable.tag_background); // tự tạo 1 drawable bo tròn nền nhạt
                tagView.setTextColor(getResources().getColor(R.color.primary));
                tagView.setPadding(24, 8, 24, 8);
                tagView.setTextSize(13);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 16, 0);
                tagView.setLayoutParams(lp);
                tagContainer.addView(tagView);
            }
        } else {
            tagContainer.setVisibility(View.GONE);
        }

        // Thông số
        if (heritage.getStats() != null) {
            ((TextView) findViewById(R.id.heritageRating)).setText("Đánh giá: " + heritage.getStats().getAverageRating());
            ((TextView) findViewById(R.id.heritageFavorites)).setText("Yêu thích: " + heritage.getStats().getTotalFavorites());
        } else {
            ((TextView) findViewById(R.id.heritageRating)).setText("Đánh giá: N/A");
            ((TextView) findViewById(R.id.heritageFavorites)).setText("Yêu thích: 0");
        }

        // Kiến trúc
        TextView architecturalTitle = findViewById(R.id.architecturalTitle);
        TextView architecturalView = findViewById(R.id.heritageArchitectural);
        String architectural = null;
        if (heritage.getAdditionalInfo() != null) {
            architectural = heritage.getAdditionalInfo().getArchitectural();
        }
        if (architectural != null && !architectural.trim().isEmpty() && !architectural.equalsIgnoreCase("null")) {
            architecturalTitle.setVisibility(View.VISIBLE);
            architecturalView.setVisibility(View.VISIBLE);
            architecturalView.setText(architectural);
        } else {
            architecturalTitle.setVisibility(View.GONE);
            architecturalView.setVisibility(View.GONE);
        }

        // Lễ hội
        TextView festivalView = findViewById(R.id.heritageFestival);
        String festival = null;
        if (heritage.getAdditionalInfo() != null) {
            festival = heritage.getAdditionalInfo().getCulturalFestival();
        }
        if (festival != null && !festival.trim().isEmpty() && !festival.equalsIgnoreCase("null")) {
            festivalView.setText("Lễ hội: " + festival);
            festivalView.setVisibility(View.VISIBLE);
        } else {
            festivalView.setVisibility(View.GONE);
        }

        // Sự kiện lịch sử
        LinearLayout eventsContainer = findViewById(R.id.heritageEventsContainer);
        eventsContainer.removeAllViews();
        if (heritage.getAdditionalInfo() != null && heritage.getAdditionalInfo().getHistoricalEvents() != null
                && !heritage.getAdditionalInfo().getHistoricalEvents().isEmpty()) {
            for (hcmute.edu.vn.heritageproject.api.models.Heritage.HistoricalEvent event : heritage.getAdditionalInfo().getHistoricalEvents()) {
                TextView eventTitle = new TextView(this);
                eventTitle.setText(event.getTitle());
                eventTitle.setTextSize(16);
                eventTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                eventTitle.setTextColor(getResources().getColor(R.color.primary));
                TextView eventDesc = new TextView(this);
                eventDesc.setText(event.getDescription());
                eventDesc.setTextSize(14);
                eventDesc.setPadding(0, 0, 0, 16);
                eventsContainer.addView(eventTitle);
                eventsContainer.addView(eventDesc);
            }
        } else {
            findViewById(R.id.heritageEventsTitle).setVisibility(View.GONE);
            eventsContainer.setVisibility(View.GONE);
        }
    }

    private void checkFavoriteStatus(String heritageId) {
        favoritesService.getCurrentUserFavorites(new FavoritesService.FavoritesServiceCallback() {
            @Override
            public void onSuccess(Favorites favorites) {
                if (favorites != null && favorites.getItems() != null) {
                    isFavorite = favorites.getItems().stream()
                            .anyMatch(item -> item.getHeritageId().equals(heritageId));
                    updateFavoriteButtonState();
                }
            }

            @Override
            public void onError(String error) {
                // Ignore error for now
            }
        });
    }

    private void toggleFavorite() {
        String heritageId = getIntent().getStringExtra("heritageId");
        if (heritageId == null)
            return;

        if (isFavorite) {
            favoritesService.removeHeritageFromFavorites(heritageId, new FavoritesService.FavoritesServiceCallback() {
                @Override
                public void onSuccess(Favorites favorites) {
                    isFavorite = false;
                    updateFavoriteButtonState();
                    Toast.makeText(HeritageDetailActivity.this,
                            "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(HeritageDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            favoritesService.addHeritageToFavorites(heritageId, new FavoritesService.FavoritesServiceCallback() {
                @Override
                public void onSuccess(Favorites favorites) {
                    isFavorite = true;
                    updateFavoriteButtonState();
                    Toast.makeText(HeritageDetailActivity.this,
                            "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(HeritageDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateFavoriteButtonState() {
        buttonFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        buttonFavorite
                .setContentDescription(isFavorite ? "Xóa khỏi danh sách yêu thích" : "Thêm vào danh sách yêu thích");
    }
}