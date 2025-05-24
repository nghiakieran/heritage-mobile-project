package hcmute.edu.vn.heritageproject.api;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service class to handle heritage API requests
 */
public class HeritageApiService {
    private static final String TAG = "HeritageApiService";
    private final OkHttpClient client;
    private static HeritageApiService instance;
    private static final int MAX_RETRIES = 2;
    
    // Interface for API response callbacks
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
    
    // Private constructor for singleton pattern
    private HeritageApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    // Singleton instance getter
    public static synchronized HeritageApiService getInstance() {
        if (instance == null) {
            instance = new HeritageApiService();
        }
        return instance;
    }
    
    /**
     * Get all heritages from the API with pagination
     */
    public void getAllHeritages(int page, int limit, String name,
                               ApiCallback<HeritageResponse> callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(ApiConfig.getBaseUrl() + ApiConfig.Endpoints.ALL_HERITAGES)
                .newBuilder()
                .addQueryParameter("page", String.valueOf(page))
                .addQueryParameter("limit", String.valueOf(limit));
        
        // Add optional parameters if provided
        if (name != null && !name.isEmpty()) {
            urlBuilder.addQueryParameter("name", name);
        }
        

        makeGetRequest(urlBuilder.build().toString(), callback);
    }
      /**
     * Get all heritages (simplified, without parameters)
     */
    public void getAllHeritages(ApiCallback<HeritageResponse> callback) {
        // Default values: first page, 50 items, all active heritages
        getAllHeritages(1, 50, null, callback);
    }
    
    /**
     * Get a specific heritage by ID
     */
    public void getHeritageById(String heritageId, ApiCallback<HeritageResponse> callback) {
        String url = ApiConfig.getBaseUrl() + ApiConfig.Endpoints.HERITAGE_BY_ID + heritageId;
        makeGetRequest(url, callback);
    }
    
    /**
     * Get a heritage by its name slug
     */
    public void getHeritageBySlug(String nameSlug, ApiCallback<HeritageResponse> callback) {
        String url = ApiConfig.getBaseUrl() + ApiConfig.Endpoints.HERITAGE_BY_SLUG + nameSlug;
        makeGetRequest(url, callback);
    }
    
    /**
     * Get heritages near the user's location
     * @param latitude User's current latitude
     * @param longitude User's current longitude
     * @param limit Number of nearest heritages to fetch
     */
    public void getNearestHeritages(double latitude, double longitude, int limit, ApiCallback<HeritageResponse> callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(ApiConfig.getBaseUrl() + ApiConfig.Endpoints.EXPLORE_HERITAGES)
                .newBuilder()
                .addQueryParameter("latitude", String.valueOf(latitude))
                .addQueryParameter("longitude", String.valueOf(longitude));
                
        if (limit > 0) {
            urlBuilder.addQueryParameter("limit", String.valueOf(limit));
        }
        
        makeGetRequest(urlBuilder.build().toString(), callback);
    }
    
    /**
     * Get all heritage names (for dropdown lists etc.)
     */
    public void getAllHeritageNames(ApiCallback<HeritageResponse> callback) {
        String url = ApiConfig.getBaseUrl() + ApiConfig.Endpoints.ALL_HERITAGE_NAMES;
        makeGetRequest(url, callback);
    }
      
    /**
     * Make a GET request to the API
     */
    private void makeGetRequest(String url, ApiCallback<HeritageResponse> callback) {
        // Use the retry mechanism with initial retry count of 0
        makeGetRequestWithRetry(url, callback, 0);
    }
    
    /**
     * Make a GET request with retry mechanism
     */
    private void makeGetRequestWithRetry(String url, ApiCallback<HeritageResponse> callback, int retryCount) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
                
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (retryCount < MAX_RETRIES) {
                    Log.w(TAG, "Request failed, retrying (" + (retryCount + 1) + "/" + MAX_RETRIES + "): " + url);
                    makeGetRequestWithRetry(url, callback, retryCount + 1);
                } else {
                    Log.e(TAG, "API request failed after " + MAX_RETRIES + " retries: " + url, e);
                    callback.onError(e);
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (retryCount < MAX_RETRIES && (response.code() >= 500 || response.code() == 429)) {
                        // Server error or rate limiting, retry
                        Log.w(TAG, "Request returned " + response.code() + ", retrying: " + url);
                        makeGetRequestWithRetry(url, callback, retryCount + 1);
                        return;
                    }
                    
                    callback.onError(new IOException("Unexpected response code: " + response.code()));
                    return;
                }
                
                if (response.body() == null) {
                    callback.onError(new IOException("Response body is null"));
                    return;
                }
                
                String responseData = response.body().string();
                try {
                    // Parse the JSON response into HeritageResponse object
                    JSONObject jsonResponse = new JSONObject(responseData);
                    HeritageResponse heritageResponse = JsonParser.parseHeritageResponse(jsonResponse);
                    callback.onSuccess(heritageResponse);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse heritage response", e);
                    callback.onError(e);
                }
            }
        });
    }
}
