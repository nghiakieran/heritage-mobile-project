package hcmute.edu.vn.heritageproject.api;

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

public class HeritageApiService {
    private static final String TAG = "HeritageApiService";
    private final OkHttpClient client;
    private static HeritageApiService instance;
    private static final int MAX_RETRIES = 2;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private HeritageApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized HeritageApiService getInstance() {
        if (instance == null) {
            instance = new HeritageApiService();
        }
        return instance;
    }

    public void getAllHeritages(int page, int limit, String name,
                                ApiCallback<HeritageResponse> callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(ApiConfig.getBaseUrl() + ApiConfig.Endpoints.ALL_HERITAGES)
                .newBuilder()
                .addQueryParameter("page", String.valueOf(page))
                .addQueryParameter("limit", String.valueOf(limit));

        if (name != null && !name.isEmpty()) {
            urlBuilder.addQueryParameter("name", name);
        }

        String url = urlBuilder.build().toString();
        Log.d(TAG, "Request URL for getAllHeritages: " + url);
        makeGetRequest(url, callback);
    }

    public void getAllHeritages(ApiCallback<HeritageResponse> callback) {
        getAllHeritages(1, 50, null, callback);
    }

    public void getHeritageById(String heritageId, ApiCallback<HeritageResponse> callback) {
        String url = ApiConfig.getBaseUrl() + ApiConfig.Endpoints.HERITAGE_BY_ID + heritageId;
        Log.d(TAG, "Request URL for getHeritageById: " + url);
        makeGetRequest(url, callback);
    }

    public void getHeritageBySlug(String nameSlug, ApiCallback<HeritageResponse> callback) {
        String url = ApiConfig.getBaseUrl() + ApiConfig.Endpoints.HERITAGE_BY_SLUG + nameSlug;
        Log.d(TAG, "Request URL for getHeritageBySlug: " + url);
        makeGetRequest(url, callback);
    }

    public void getNearestHeritages(double latitude, double longitude, int limit, ApiCallback<HeritageResponse> callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(ApiConfig.getBaseUrl() + ApiConfig.Endpoints.EXPLORE_HERITAGES)
                .newBuilder()
                .addQueryParameter("latitude", String.valueOf(latitude))
                .addQueryParameter("longitude", String.valueOf(longitude));

        if (limit > 0) {
            urlBuilder.addQueryParameter("limit", String.valueOf(limit));
        }

        String url = urlBuilder.build().toString();
        Log.d(TAG, "Request URL for getNearestHeritages: " + url);
        makeGetRequest(url, callback);
    }

    public void getAllHeritageNames(ApiCallback<HeritageResponse> callback) {
        String url = ApiConfig.getBaseUrl() + ApiConfig.Endpoints.ALL_HERITAGE_NAMES;
        Log.d(TAG, "Request URL for getAllHeritageNames: " + url);
        makeGetRequest(url, callback);
    }

    private void makeGetRequest(String url, ApiCallback<HeritageResponse> callback) {
        makeGetRequestWithRetry(url, callback, 0);
    }

    private void makeGetRequestWithRetry(String url, ApiCallback<HeritageResponse> callback, int retryCount) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed: " + e.getMessage());
                if (retryCount < MAX_RETRIES) {
                    Log.w(TAG, "Request failed, retrying (" + (retryCount + 1) + "/" + MAX_RETRIES + "): " + url);
                    makeGetRequestWithRetry(url, callback, retryCount + 1);
                } else {
                    Log.e(TAG, "API request failed after " + MAX_RETRIES + " retries: " + url);
                    callback.onError(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Response code: " + response.code());
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unsuccessful response: " + response.code());
                    if (retryCount < MAX_RETRIES && (response.code() >= 500 || response.code() == 429)) {
                        Log.w(TAG, "Request returned " + response.code() + ", retrying: " + url);
                        makeGetRequestWithRetry(url, callback, retryCount + 1);
                        return;
                    }
                    callback.onError(new IOException("Unexpected response code: " + response.code()));
                    return;
                }

                if (response.body() == null) {
                    Log.e(TAG, "Response body is null");
                    callback.onError(new IOException("Response body is null"));
                    return;
                }

                String responseData = response.body().string();
                Log.d(TAG, "Response body: " + responseData);
                try {
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