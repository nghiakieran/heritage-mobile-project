package hcmute.edu.vn.heritageproject.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.api.HeritageApiService;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;
import hcmute.edu.vn.heritageproject.utils.NetworkUtils;
import hcmute.edu.vn.heritageproject.api.models.AppCache;

public class HeritageDetailActivity extends AppCompatActivity {

    private static final String TAG = "HeritageDetailActivity";
    private ImageView heritageImage;
    private TextView heritageName;
    private TextView heritageLocation;
    private TextView heritageDescription;
    private TextView heritageRating;
    private TextView heritageFavorites;
    private HeritageApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heritage_detail);

        // Initialize views
        heritageImage = findViewById(R.id.heritageImage);
        heritageName = findViewById(R.id.heritageName);
        heritageLocation = findViewById(R.id.heritageLocation);
        heritageDescription = findViewById(R.id.heritageDescription);
        heritageRating = findViewById(R.id.heritageRating);
        heritageFavorites = findViewById(R.id.heritageFavorites);

        // Initialize API service
        apiService = HeritageApiService.getInstance();

        // Get heritage ID from intent
        String heritageId = getIntent().getStringExtra("heritageId");
        Log.d(TAG, "Received heritageId: " + (heritageId != null ? heritageId : "null"));
        if (heritageId == null || heritageId.isEmpty()) {
            Toast.makeText(this, R.string.no_heritage_id, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "heritageId is null or empty");
            finish();
            return;
        }

        // Ưu tiên lấy từ cache
        Heritage heritageFromCache = null;
        java.util.List<hcmute.edu.vn.heritageproject.api.models.Heritage> cached = AppCache.getHeritageList();
        if (cached != null && !cached.isEmpty()) {
            for (hcmute.edu.vn.heritageproject.api.models.Heritage h : cached) {
                if (heritageId.equals(h.getId())) {
                    heritageFromCache = h;
                    break;
                }
            }
        }
        if (heritageFromCache != null) {
            Log.d(TAG, "Found heritage in cache: " + heritageFromCache.getName());
            displayHeritageDetails(heritageFromCache);
            return;
        }

        // Nếu không có trong cache thì gọi API như cũ
        if (NetworkUtils.isNetworkAvailable(this)) {
            Log.d(TAG, "Network available, loading heritage details for ID: " + heritageId);
            loadHeritageDetails(heritageId);
        } else {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No network connection");
            finish();
        }
    }

    private void loadHeritageDetails(String heritageId) {
        Log.d(TAG, "Calling API getHeritageById with ID: " + heritageId);
        apiService.getHeritageById(heritageId, new HeritageApiService.ApiCallback<HeritageResponse>() {
            @Override
            public void onSuccess(HeritageResponse result) {
                Log.d(TAG, "API Response: Success=" + result.isSuccess() +
                        ", Message=" + result.getMessage() +
                        ", Heritages=" + (result.getHeritages() != null ? result.getHeritages().size() : "null") +
                        ", ID=" + (result.getId() != null ? result.getId() : "null"));
                runOnUiThread(() -> {
                    Heritage heritage;
                    if (result.isSuccess() && result.getHeritages() != null && !result.getHeritages().isEmpty()) {
                        heritage = result.getHeritages().get(0);
                    } else if (result.getId() != null) {
                        // Xử lý response trực tiếp từ backend
                        heritage = new Heritage();
                        heritage.setId(result.getId());
                        heritage.setName(result.getName());
                        heritage.setDescription(result.getDescription());
                        heritage.setImages(result.getImages());
                        heritage.setLocation(result.getLocation());
                        heritage.setCoordinates(result.getCoordinates());
                        heritage.setStats(result.getStats());
                        heritage.setKnowledgeTestId(result.getKnowledgeTestId());
                        heritage.setLeaderboardId(result.getLeaderboardId());
                        heritage.setLeaderboardSummary(result.getLeaderboardSummary());
                        heritage.setKnowledgeTestSummary(result.getKnowledgeTestSummary());
                        heritage.setRolePlayIds(result.getRolePlayIds());
                        heritage.setAdditionalInfo(result.getAdditionalInfo());
                        heritage.setStatus(result.getStatus());
                        heritage.setPopularTags(result.getPopularTags());
                        heritage.setLocationSlug(result.getLocationSlug());
                        heritage.setNameSlug(result.getNameSlug());
                        heritage.setTagsSlug(result.getTagsSlug());
                    } else {
                        Log.e(TAG, "Invalid response: Success=" + result.isSuccess() +
                                ", Message=" + result.getMessage() +
                                ", Heritages=" + (result.getHeritages() != null ? result.getHeritages().size() : "null"));
                        Toast.makeText(HeritageDetailActivity.this,
                                R.string.cannot_load_details,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d(TAG, "Heritage Data: ID=" + heritage.getId() +
                            ", Name=" + (heritage.getName() != null ? heritage.getName() : "null") +
                            ", Location=" + (heritage.getLocation() != null ? heritage.getLocation() : "null") +
                            ", Images=" + (heritage.getImages() != null ? heritage.getImages().size() : "null"));
                    displayHeritageDetails(heritage);
                });
            }

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Failed to load heritage details: " + e.getMessage(), e);
                    Toast.makeText(HeritageDetailActivity.this,
                            getString(R.string.load_error, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayHeritageDetails(Heritage heritage) {
        Log.d(TAG, "Displaying heritage details for ID: " + heritage.getId());
        // Set heritage name
        heritageName.setText(heritage.getName() != null ? heritage.getName() : getString(R.string.unknown_name));

        // Set heritage location
        heritageLocation.setText(heritage.getLocation() != null ? heritage.getLocation() : getString(R.string.unknown_location));

        // Set heritage description
        heritageDescription.setText(heritage.getDescription() != null ? heritage.getDescription() : getString(R.string.unknown_description));

        // Set heritage image
        if (heritage.getImages() != null && !heritage.getImages().isEmpty()) {
            Log.d(TAG, "Loading image: " + heritage.getImages().get(0));
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .transform(new RoundedCorners(8));
            Glide.with(this)
                    .load(heritage.getImages().get(0))
                    .apply(requestOptions)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(heritageImage);
        } else {
            Log.d(TAG, "No images available for ID: " + heritage.getId());
            heritageImage.setImageResource(R.drawable.placeholder_image);
        }

        // Set heritage stats
        if (heritage.getStats() != null) {
            heritageRating.setText(getString(R.string.rating, heritage.getStats().getAverageRating()));
            heritageFavorites.setText(getString(R.string.favorites, heritage.getStats().getTotalFavorites()));
        } else {
            Log.d(TAG, "No stats available for ID: " + heritage.getId());
            heritageRating.setText(R.string.rating_na);
            heritageFavorites.setText(R.string.favorites_zero);
        }
    }
}