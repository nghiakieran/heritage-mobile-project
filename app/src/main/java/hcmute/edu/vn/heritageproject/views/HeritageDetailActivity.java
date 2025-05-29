package hcmute.edu.vn.heritageproject.views;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        if (heritageId == null) {
            Toast.makeText(this, "Không tìm thấy ID di tích", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load heritage details
        if (NetworkUtils.isNetworkAvailable(this)) {
            loadHeritageDetails(heritageId);
        } else {
            Toast.makeText(this, "Không có kết nối mạng. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }    private void loadHeritageDetails(String heritageId) {
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
    }    private void displayHeritageDetails(Heritage heritage) {
        Log.d(TAG, "Displaying heritage details - Name: " + heritage.getName() + ", ID: " + heritage.getId());
        
        // Set heritage name
        heritageName.setText(heritage.getName());

        // Set heritage location
        heritageLocation.setText(heritage.getLocation());

        // Set heritage description
        heritageDescription.setText(heritage.getDescription());

        // Set heritage image
        if (heritage.getImages() != null && !heritage.getImages().isEmpty()) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(8));
            Glide.with(this)
                    .load(heritage.getImages().get(0))
                    .apply(requestOptions)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(heritageImage);
        } else {
            heritageImage.setImageResource(R.drawable.placeholder_image);
        }

        // Set heritage stats
        if (heritage.getStats() != null) {
            heritageRating.setText("Đánh giá: " + heritage.getStats().getAverageRating());
            heritageFavorites.setText("Yêu thích: " + heritage.getStats().getTotalFavorites());
        } else {
            heritageRating.setText("Đánh giá: N/A");
            heritageFavorites.setText("Yêu thích: 0");
        }
    }
}