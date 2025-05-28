package hcmute.edu.vn.heritageproject.api;

import android.os.Build;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.heritageproject.api.ApiConfig.Endpoints;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiConfig {    // URL API - Cập nhật theo API thực tế
    private static final String PRODUCTION_BASE_URL = "http://192.168.1.x:8017/v1/"; // Thay "192.168.1.x" bằng IP thực của máy chủ
    private static final String EMULATOR_BASE_URL = "http://192.168.1.152:8017/v1/"; // 10.0.2.2 trỏ đến localhost máy host từ emulator
    private static final String DEVICE_DEBUG_URL = "http://192.168.1.152:8017/v1/"; // URL cho thiết bị thật trong mạng LAN
    
    public static String getBaseUrl() {
        if (isEmulator()) {
            return EMULATOR_BASE_URL;
        } else {
            // Nếu đang chạy trên thiết bị thật, sử dụng DEVICE_DEBUG_URL trong quá trình phát triển
            // hoặc PRODUCTION_BASE_URL nếu đã triển khai
            return DEVICE_DEBUG_URL;
        }
    }
      // API endpoints - Cập nhật theo route thực tế từ backend
    public static class Endpoints {
        public static final String EXPLORE_HERITAGES = "heritages/explore";
        public static final String ALL_HERITAGES = "heritages";
        public static final String HERITAGE_BY_ID = "heritages/id/";
        public static final String HERITAGE_BY_SLUG = "heritages/";
        public static final String ALL_HERITAGE_NAMES = "heritages/all-name";
    }
      // Phát hiện nếu đang chạy trên emulator (phương pháp đáng tin cậy hơn)
    private static boolean isEmulator() {
        return Build.BRAND.startsWith("generic") 
                || Build.BRAND.startsWith("Android")
                || Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_gphone64")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }
    
    // Phương thức hữu ích để debug thông tin thiết bị
    public static String getDeviceInfo() {
        return "Brand: " + Build.BRAND +
               "\nModel: " + Build.MODEL + 
               "\nProduct: " + Build.PRODUCT + 
               "\nDevice: " + Build.DEVICE +
               "\nHardware: " + Build.HARDWARE +
               "\nFingerprint: " + Build.FINGERPRINT +
               "\nManufacturer: " + Build.MANUFACTURER +
               "\nIs Emulator: " + isEmulator();
    }
    
    /**
     * Kiểm tra kết nối đến URL của API
     * @param callback Callback nhận kết quả kiểm tra
     */
    public static void checkApiConnection(ApiConnectionCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        
        Request request = new Request.Builder()
                .url(getBaseUrl() + "status")
                .head() // HEAD request để kiểm tra kết nối mà không tải dữ liệu
                .build();
                
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onConnectionResult(false, e.getMessage(), getBaseUrl());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onConnectionResult(response.isSuccessful(), 
                        "Response code: " + response.code(), getBaseUrl());
            }
        });
    }
    
    /**
     * Interface callback cho việc kiểm tra kết nối API
     */
    public interface ApiConnectionCallback {
        void onConnectionResult(boolean isConnected, String message, String testedUrl);
    }
}
