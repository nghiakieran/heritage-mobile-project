package hcmute.edu.vn.heritageproject.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import hcmute.edu.vn.heritageproject.api.ApiConfig;
import hcmute.edu.vn.heritageproject.api.HeritageApiService;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageNameResponse;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;
import hcmute.edu.vn.heritageproject.models.PopularMonument;
import hcmute.edu.vn.heritageproject.repository.MonumentRepository;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatService {
    private static final String TAG = "ChatService";
    private static final String API_KEY = "AIzaSyB6F4AejtEla-RmnbFS9dtAFaaX_fPrjpU";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";
    
    private final OkHttpClient client;
    private final Context context;
    
    public ChatService(OkHttpClient client, Context context) {
        this.client = client;
        this.context = context;
    }
    
    public void callGemini(String prompt, Callback callback) {
        try {
            // Tạo JSONObject cho request
            JSONObject contentPart = new JSONObject();
            contentPart.put("text", prompt);

            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(contentPart));

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", new JSONArray().put(content));

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
            );

            String url = GEMINI_URL + API_KEY;
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Log.d(TAG, "callGemini: " + request);
            Log.d(TAG, "callGemini body: " + body);
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create Gemini request", e);
        } 
    }
    public void getHeritageData(String userInput, final Callback callback) {
        HeritageApiService apiService = HeritageApiService.getInstance();

        if (userInput == null || userInput.isEmpty()) {
            // Lấy tất cả di tích nếu không có từ khóa
            Log.d(TAG, "Lấy tất cả di tích vì không có từ khóa");
            apiService.getAllHeritages(1, 50, null,
                    new HeritageApiService.ApiCallback<HeritageResponse>() {
                        @Override
                        public void onSuccess(HeritageResponse result) {
                            processHeritageResponse(result, userInput, callback);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "API call failed, falling back to local data", e);
                            fallbackToLocalData(e, userInput, callback);
                        }
                    });
            return;
        }

        // Tìm kiếm với input người dùng gốc
        Log.d(TAG, "Tìm kiếm di tích với từ khóa gốc: " + userInput);
        apiService.getAllHeritages(1, 50, userInput,
                new HeritageApiService.ApiCallback<HeritageResponse>() {
                    @Override
                    public void onSuccess(HeritageResponse result) {
                        if (result != null && result.getHeritages() != null && !result.getHeritages().isEmpty()) {
                            // Tìm thấy dữ liệu phù hợp
                            Log.d(TAG, "Tìm thấy " + result.getHeritages().size() + " di tích phù hợp với từ khóa gốc");
                            processHeritageResponse(result, userInput, callback);
                        } else {
                            // Không tìm thấy, lấy toàn bộ dữ liệu di tích và gọi Gemini
                            Log.d(TAG, "Không tìm thấy di tích với từ khóa gốc, lấy toàn bộ dữ liệu di tích");
                            apiService.getAllHeritages(1, 50, null,
                                    new HeritageApiService.ApiCallback<HeritageResponse>() {
                                        @Override
                                        public void onSuccess(HeritageResponse allHeritagesResult) {
                                            Log.d(TAG, "Đã lấy được " + (allHeritagesResult.getHeritages() != null ? allHeritagesResult.getHeritages().size() : 0) + " di tích");
                                            callGeminiForAnalysis(userInput, allHeritagesResult, callback);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.e(TAG, "Lỗi khi lấy tất cả di tích", e);
                                            fallbackToLocalData(e, userInput, callback);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Lỗi khi tìm kiếm di tích với từ khóa gốc", e);
                        // Lấy toàn bộ dữ liệu di tích và gọi Gemini
                        apiService.getAllHeritages(1, 50, null,
                                new HeritageApiService.ApiCallback<HeritageResponse>() {
                                    @Override
                                    public void onSuccess(HeritageResponse allHeritagesResult) {
                                        Log.d(TAG, "Đã lấy được " + (allHeritagesResult.getHeritages() != null ? allHeritagesResult.getHeritages().size() : 0) + " di tích");
                                        callGeminiForAnalysis(userInput, allHeritagesResult, callback);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e(TAG, "Lỗi khi lấy tất cả di tích", e);
                                        fallbackToLocalData(e, userInput, callback);
                                    }
                                });
                    }
                });
    }

    private Response createMockResponse(String data) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(data, JSON);
        Request request = new Request.Builder()
                .url("http://localhost")
                .post(body)
                .build();
        return new Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_2)
                .code(200)
                .message("OK")
                .body(okhttp3.ResponseBody.create(data, JSON))
                .build();
    }
    private void callGeminiForAnalysis(String userInput, HeritageResponse heritagesResult, Callback callback) {
        // Tạo chuỗi dữ liệu di tích
        StringBuilder heritageData = new StringBuilder();
        if (heritagesResult != null && heritagesResult.getHeritages() != null && !heritagesResult.getHeritages().isEmpty()) {
            heritageData.append("Danh sách di tích:\n");
            for (Heritage heritage : heritagesResult.getHeritages()) {
                heritageData.append(String.format("- Tên: %s\n  Địa điểm: %s\n  Mô tả: %s\n",
                        heritage.getName(),
                        heritage.getLocation() != null ? heritage.getLocation() : "Không có thông tin",
                        heritage.getDescription() != null ? heritage.getDescription() : "Không có thông tin"));
            }
        } else {
            heritageData.append("Không có dữ liệu di tích nào.");
        }

        // Tạo prompt cho Gemini
        String prompt = "Người dùng hỏi: '" + userInput + "'. " +
                "Dưới đây là danh sách tất cả các di tích văn hóa Việt Nam trong cơ sở dữ liệu:\n" +
                heritageData.toString() + "\n" +
                "Hãy phân tích câu hỏi của người dùng và tìm di tích phù hợp nhất trong danh sách trên dựa trên tên, địa điểm hoặc mô tả. " +
                "Trả về phản hồi ngắn gọn, lịch sự bằng tiếng Việt, cung cấp thông tin về di tích nếu tìm thấy. " +
                "Nếu không tìm thấy di tích phù hợp, trả về: 'Xin lỗi, tôi không biết thông tin về di tích này.'";

        callGemini(prompt, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String aiResponseBody = response.body().string();
                    Log.d(TAG, "Gemini Analysis Response: " + aiResponseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(aiResponseBody);
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        String reply = candidates.getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        // Tạo phản hồi giả để tương thích với callback
                        String formattedData = reply.equals("Xin lỗi, tôi không biết thông tin về di tích này.") ?
                                "Không tìm thấy di tích phù hợp với từ khóa \"" + userInput + "\"." :
                                reply;
                        Response mockResponse = createMockResponse(formattedData);
                        callback.onResponse(null, mockResponse);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi xử lý phản hồi Gemini", e);
                        fallbackToLocalData(e, userInput, callback);
                    }
                } else {
                    Log.e(TAG, "Gemini API error: " + (response.body() != null ? response.code() : "null response"));
                    fallbackToLocalData(new IOException("Gemini API error"), userInput, callback);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Gemini API call failed", e);
                fallbackToLocalData(e, userInput, callback);
            }
        });
    }
    /**
     * Process heritage response and create API callback response
     */
    private void processHeritageResponse(HeritageResponse heritageResponse, String userInput, Callback callback) {
        try {
            // Tạo chuỗi dữ liệu di tích
            StringBuilder heritageData = new StringBuilder();
            if (heritageResponse.getHeritages() != null && !heritageResponse.getHeritages().isEmpty()) {
                heritageData.append("Dữ liệu di tích:\n");
                for (Heritage heritage : heritageResponse.getHeritages()) {
                    heritageData.append(String.format("- Tên: %s\n  Địa điểm: %s\n  Mô tả: %s\n",
                            heritage.getName(),
                            heritage.getLocation() != null ? heritage.getLocation() : "Không có thông tin",
                            heritage.getDescription() != null ? heritage.getDescription() : "Không có thông tin"));
                }
            } else {
                heritageData.append("Không có dữ liệu di tích nào.");
            }

            // Tạo prompt cho Gemini
            String prompt = "Người dùng hỏi: '" + userInput + "'. " +
                    "Dữ liệu di tích:\n" + heritageData.toString() + "\n" +
                    "Hãy trả lời câu hỏi của người dùng một cách ngắn gọn, lịch sự bằng tiếng Việt, dựa trên dữ liệu di tích được cung cấp.";

            callGemini(prompt, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String aiResponseBody = response.body().string();
                        Log.d(TAG, "Gemini Response: " + aiResponseBody);

                        try {
                            JSONObject jsonResponse = new JSONObject(aiResponseBody);
                            JSONArray candidates = jsonResponse.getJSONArray("candidates");
                            String reply = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            // Tạo phản hồi giả
                            Response mockResponse = createMockResponse(reply);
                            callback.onResponse(null, mockResponse);
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi xử lý phản hồi Gemini", e);
                            // Fallback: sử dụng dữ liệu gốc
                            String fallbackData = prepareHeritageData(createHeritageJsonArray(heritageResponse), userInput);
                            Response mockResponse = createMockResponse(fallbackData);
                            callback.onResponse(null, mockResponse);
                        }
                    } else {
                        Log.e(TAG, "Gemini API error: " + (response.body() != null ? response.code() : "null response"));
                        String fallbackData = prepareHeritageData(createHeritageJsonArray(heritageResponse), userInput);
                        Response mockResponse = createMockResponse(fallbackData);
                        callback.onResponse(null, mockResponse);
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Gemini API call failed", e);
                    String fallbackData = prepareHeritageData(createHeritageJsonArray(heritageResponse), userInput);
                    Response mockResponse = createMockResponse(fallbackData);
                    try {
                        callback.onResponse(null, mockResponse);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error processing heritage data", e);
            fallbackToLocalData(e, userInput, callback);
        }
    }

    private JSONArray createHeritageJsonArray(HeritageResponse heritageResponse) {
        JSONArray heritagesArray = new JSONArray();
        if (heritageResponse.getHeritages() != null) {
            for (Heritage heritage : heritageResponse.getHeritages()) {
                try {
                    JSONObject heritageObj = new JSONObject();
                    heritageObj.put("name", heritage.getName());
                    heritageObj.put("description", heritage.getDescription());
                    heritageObj.put("location", heritage.getLocation());
                    heritagesArray.put(heritageObj);
                } catch (JSONException e) {
                    Log.e(TAG, "Error creating heritage JSON", e);
                }
            }
        }
        return heritagesArray;
    }
      public String prepareHeritageData(JSONArray heritagesArray, String userInput) {
        StringBuilder heritageSummary = new StringBuilder();
        boolean foundMatch = false;

        try {
            // Nếu người dùng có nhập từ khóa, thử tìm di tích phù hợp
            if (userInput != null && !userInput.isEmpty()) {
                String userInputLower = userInput.toLowerCase();
                
                // Tìm các di tích phù hợp với từ khóa người dùng nhập
                for (int i = 0; i < heritagesArray.length(); i++) {
                    JSONObject heritage = heritagesArray.getJSONObject(i);
                    String name = heritage.getString("name");
                    String description = heritage.getString("description");
                    String location = heritage.getString("location");
                    
                    boolean matchesName = name.toLowerCase().contains(userInputLower);
                    boolean matchesDescription = description.toLowerCase().contains(userInputLower);
                    boolean matchesLocation = location.toLowerCase().contains(userInputLower);
                    
                    if (matchesName || matchesDescription || matchesLocation) {
                        foundMatch = true;
                        heritageSummary.append(String.format("- %s (Địa điểm: %s): %s\n",
                                name, location, description));
                    }
                }
            }
            
            // Nếu không tìm thấy kết quả phù hợp hoặc không có từ khóa tìm kiếm, hiển thị tất cả di tích
            if (!foundMatch) {
                if (userInput != null && !userInput.isEmpty()) {
                    heritageSummary.append("Không tìm thấy di tích phù hợp với từ khóa \"" + userInput + "\". Dưới đây là danh sách các di tích hiện có:\n\n");
                } else {
                    heritageSummary.append("Danh sách các di tích văn hóa:\n\n");
                }
                
                // Giới hạn số lượng di tích hiển thị để tránh vượt quá giới hạn tin nhắn
                int displayLimit = 20; // Giới hạn số lượng di tích hiển thị
                int totalItems = heritagesArray.length();
                
                // Thêm thông tin về tổng số di tích
                heritageSummary.append("Tìm thấy " + totalItems + " di tích. ");
                if (totalItems > displayLimit) {
                    heritageSummary.append("Hiển thị " + displayLimit + " di tích đầu tiên:\n\n");
                } else {
                    heritageSummary.append("\n\n");
                }
                
                // Hiển thị danh sách di tích với giới hạn
                for (int i = 0; i < Math.min(displayLimit, totalItems); i++) {
                    JSONObject heritage = heritagesArray.getJSONObject(i);
                    String name = heritage.getString("name");
                    // Giới hạn độ dài mô tả để tin nhắn không quá dài
                    String description = heritage.getString("description");
                    if (description.length() > 200) {
                        description = description.substring(0, 197) + "...";
                    }
                    String location = heritage.getString("location");
                    
                    heritageSummary.append(String.format("%d. %s (Địa điểm: %s): %s\n\n",
                            i + 1, name, location, description));
                }
                
                // Nếu có nhiều hơn số lượng giới hạn, thông báo
                if (totalItems > displayLimit) {
                    heritageSummary.append("... và " + (totalItems - displayLimit) + " di tích khác. Hãy hỏi về một di tích cụ thể để biết thêm chi tiết.\n");
                }
            }
            
            // Thêm gợi ý cho người dùng
            if (heritagesArray.length() > 0) {
                heritageSummary.append("\n(Bạn có thể hỏi chi tiết về một di tích cụ thể trong danh sách trên)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing heritage data", e);
            return "Không thể xử lý dữ liệu di tích. Lỗi: " + e.getMessage();
        }
        
        return heritageSummary.length() > 0 ? heritageSummary.toString() : "Không có di tích nào trong danh sách.";
    }
    
    /**
     * Get error message based on exception
     */
    public String getErrorMessage(Exception e) {
        if (e instanceof IOException) {
            return "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.";
        } else if (e instanceof JSONException) {
            return "Lỗi xử lý dữ liệu từ máy chủ.";
        } else {
            return "Đã xảy ra lỗi. Vui lòng thử lại sau.";
        }
    }
    
    /**
     * Check if Gemini API is available
     */
    public void checkGeminiAvailability(Context context, final ApiAvailabilityCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onResult(false);
            return;
        }
        
        try {
            // Simple ping to check API availability
            JSONObject contentPart = new JSONObject();
            contentPart.put("text", "Hello");

            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(contentPart));

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", new JSONArray().put(content));

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
            );

            String url = GEMINI_URL + API_KEY;
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onResult(false);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    callback.onResult(response.isSuccessful());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to check Gemini availability", e);
            callback.onResult(false);
        }
    }
    
    /**
     * Kiểm tra kết nối đến API Heritage trước khi thực hiện các cuộc gọi API
     */
    public void checkApiConnection(final ApiConnectionCallback callback) {
        ApiConfig.checkApiConnection(new ApiConfig.ApiConnectionCallback() {
            @Override
            public void onConnectionResult(boolean isConnected, String message, String testedUrl) {
                Log.d(TAG, "API Connection: " + (isConnected ? "Success" : "Failed"));
                Log.d(TAG, "URL tested: " + testedUrl);
                Log.d(TAG, "Message: " + message);
                Log.d(TAG, "Device info: " + ApiConfig.getDeviceInfo());
                
                if (callback != null) {
                    callback.onConnectionResult(isConnected, message, testedUrl);
                }
            }
        });
    }
    
    /**
     * Interface callback cho việc kiểm tra kết nối API
     */
    public interface ApiConnectionCallback {
        void onConnectionResult(boolean isConnected, String message, String testedUrl);
    }
    
    // Interface for API availability check
    public interface ApiAvailabilityCallback {
        void onResult(boolean isAvailable);
    }
    
    /**
     * Tìm tên di tích phù hợp từ input của người dùng
     * @param userInput Câu hỏi của người dùng
     * @param namesResult Kết quả từ API getAllHeritageNames
     * @return Tên di tích phù hợp hoặc null nếu không tìm thấy
     */
    private String findMatchingHeritageName(String userInput, HeritageResponse namesResult) {
        if (namesResult == null || namesResult.getHeritages() == null || namesResult.getHeritages().isEmpty()) {
            Log.d(TAG, "Không có danh sách di tích");
            return null;
        }

        // Chuẩn hóa input của người dùng
        String normalizedInput = removeDiacritics(userInput.toLowerCase())
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .trim()
                .replaceAll("\\s+", " ");

        // Loại bỏ các prefix phổ biến
        String[] commonPrefixes = {
                "cho tôi biết về", "tôi muốn tìm hiểu về", "tôi cần thông tin về",
                "thông tin về", "giới thiệu về", "cho tôi thông tin về",
                "nói về", "kể về", "là gì", "ở đâu"
        };

        for (String prefix : commonPrefixes) {
            if (normalizedInput.startsWith(prefix)) {
                normalizedInput = normalizedInput.substring(prefix.length()).trim();
            } else if (normalizedInput.contains(" " + prefix + " ")) {
                normalizedInput = normalizedInput.replace(" " + prefix + " ", " ").trim();
            }
        }

        Log.d(TAG, "Chuỗi đã chuẩn hóa: " + normalizedInput);

        // Tokenize input
        String[] inputTokens = normalizedInput.split("\\s+");

        // Duyệt qua tất cả các di tích và tìm sự trùng khớp
        double bestScore = 0.0;
        String bestMatch = null;

        for (Heritage heritage : namesResult.getHeritages()) {
            String heritageName = heritage.getName();
            String normalizedName = removeDiacritics(heritageName.toLowerCase());

            // Kiểm tra từng token
            for (String token : inputTokens) {
                if (token.length() < 3) continue; // Bỏ qua token ngắn
                if (normalizedName.contains(token) || token.contains(normalizedName)) {
                    // Trùng khớp trực tiếp
                    return heritageName;
                }
                double similarity = calculateSimilarity(token, normalizedName);
                if (similarity > 0.5 && similarity > bestScore) { // Giảm ngưỡng để tăng độ nhạy
                    bestScore = similarity;
                    bestMatch = heritageName;
                }
            }

            // Kiểm tra thêm description và location nếu có
            String description = heritage.getDescription() != null ? removeDiacritics(heritage.getDescription().toLowerCase()) : "";
            String location = heritage.getLocation() != null ? removeDiacritics(heritage.getLocation().toLowerCase()) : "";
            for (String token : inputTokens) {
                if (token.length() < 3) continue;
                if (description.contains(token) || location.contains(token)) {
                    return heritageName;
                }
            }
        }

        Log.d(TAG, "Tên di tích phù hợp nhất: " + bestMatch + " (Score: " + bestScore + ")");
        return bestMatch;
    }
    /**
     * Loại bỏ dấu tiếng Việt từ một chuỗi
     */
    private String removeDiacritics(String text) {
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
    
    /**
     * Tính độ tương đồng giữa 2 chuỗi (0-1)
     */
    private double calculateSimilarity(String s1, String s2) {
        int max = Math.max(s1.length(), s2.length());
        if (max == 0) return 1.0; // Hai chuỗi rỗng được coi là giống nhau
        
        return 1.0 - (double) calculateLevenshteinDistance(s1, s2) / max;
    }
    
    /**
     * Tính khoảng cách Levenshtein (số thay đổi cần thiết để biến chuỗi này thành chuỗi khác)
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Fallback to local data when API calls fail
     */
    private void fallbackToLocalData(Exception e, String userInput, Callback callback) {
        Log.e(TAG, "API search failed, falling back to local data", e);
        // Fall back to local data when API fails
        List<Heritage> matchingHeritages = LocalHeritageDataSource.searchLocalHeritages(userInput);
        HeritageResponse fallbackResponse = new HeritageResponse();
        fallbackResponse.setSuccess(true);
        fallbackResponse.setMessage("Local data loaded");
        fallbackResponse.setHeritages(matchingHeritages);
        
        processHeritageResponse(fallbackResponse, userInput, callback);
    }
}
