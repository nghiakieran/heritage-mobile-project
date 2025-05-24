package hcmute.edu.vn.heritageproject.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import java.io.IOException;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.utils.ChatService;
import hcmute.edu.vn.heritageproject.utils.NetworkUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    
    private EditText inputText;
    private Button sendBtn;
    private LinearLayout chatLayout;
    private ScrollView scrollView;
    private OkHttpClient client;
    private ChatService chatService;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        // Inflate layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputText = view.findViewById(R.id.inputText);
        sendBtn = view.findViewById(R.id.sendBtn);
        chatLayout = view.findViewById(R.id.chatLayout);
        scrollView = view.findViewById(R.id.scrollView);
          // Tạo OkHttpClient với cấu hình thời gian chờ dài hơn
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        chatService = new ChatService(client, getContext());

        // Add welcome message
        addMessage("Heritage Assistant: Xin chào! Tôi có thể giúp gì cho bạn về các di tích lịch sử Việt Nam?");
        
        // Check API availability
        checkApiAvailability();

        sendBtn.setOnClickListener(v -> {
            String userInput = inputText.getText().toString().trim();
            if (userInput.isEmpty()) return;

            addMessage("Bạn: " + userInput);
            inputText.setText("");

            // Call API to get heritage data
            fetchHeritages(userInput);
        });

    }
    private void fetchHeritages(String userInput) {
        if (getContext() == null) return;

        showTypingIndicator();

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(),
                            "Đang hoạt động trong chế độ ngoại tuyến với dữ liệu cục bộ",
                            Toast.LENGTH_SHORT).show());
        }

        chatService.getHeritageData(userInput, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (getActivity() == null) return;

                removeTypingIndicator();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        Log.d(TAG, "Heritage data response: " + responseData);

                        if (responseData.contains("Không tìm thấy di tích phù hợp") ||
                                responseData.contains("Xin lỗi, tôi không biết thông tin về di tích này.")) {
                            getActivity().runOnUiThread(() ->
                                    addMessage("Heritage Assistant: Xin lỗi, tôi không tìm thấy thông tin về di tích nào phù hợp với '" + userInput + "'. Hãy thử nhập tên di tích cụ thể hơn."));
                            return;
                        }

                        getActivity().runOnUiThread(() -> addMessage("Heritage Assistant: " + responseData));
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response", e);
                        getActivity().runOnUiThread(() ->
                                addMessage("Heritage Assistant: Xin lỗi, tôi đang gặp sự cố khi đọc dữ liệu."));
                    }
                } else {
                    getActivity().runOnUiThread(() ->
                            addMessage("Heritage Assistant: Xin lỗi, tôi không thể lấy thông tin di tích. Hãy thử lại hoặc kiểm tra kết nối mạng."));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;

                removeTypingIndicator();
                getActivity().runOnUiThread(() -> {
                    Log.e(TAG, "API call failed", e);
                    addMessage("Heritage Assistant: Xin lỗi, tôi đang gặp sự cố kết nối đến dữ liệu di tích. Hãy thử lại sau.");
                });
            }
        });
    }
      // Hàm để cắt ngắn phản hồi nếu quá dài
    private String truncateResponse(String response, int maxLength) {
        if (response == null) {
            return "Không có dữ liệu";
        }
        
        if (response.length() <= maxLength) {
            return response;
        }
        
        // Đếm số di tích trong phản hồi
        int heritageCount = countOccurrences(response, "- ") + countOccurrences(response, "1. ");
        
        // Tìm vị trí của dấu xuống dòng gần nhất trước maxLength
        int cutIndex = response.lastIndexOf("\n", maxLength);
        if (cutIndex == -1) {
            cutIndex = maxLength;
        }
        
        return response.substring(0, cutIndex) + 
               "\n\n... (Hiển thị một phần của " + heritageCount + " di tích. Hãy hỏi cụ thể về một di tích để biết thêm chi tiết)";
    }
    
    // Đếm số lần xuất hiện của một chuỗi con trong chuỗi
    private int countOccurrences(String str, String subStr) {
        int count = 0;
        int lastIndex = 0;
        
        while (lastIndex != -1) {
            lastIndex = str.indexOf(subStr, lastIndex);
            
            if (lastIndex != -1) {
                count++;
                lastIndex += subStr.length();
            }
        }
        
        return count;
    }
    
    private void showTypingIndicator() {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            TextView typingIndicator = new TextView(getContext());
            typingIndicator.setId(R.id.typing_indicator);
            typingIndicator.setText("Heritage Assistant đang nhập...");
            typingIndicator.setPadding(16, 16, 16, 16);
            chatLayout.addView(typingIndicator);
            scrollToBottom();
        });
    }
    
    private void removeTypingIndicator() {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            View indicator = chatLayout.findViewById(R.id.typing_indicator);
            if (indicator != null) {
                chatLayout.removeView(indicator);
            }
        });
    }

    private void addMessage(String msg) {
        TextView tv = new TextView(getContext());
        tv.setText(msg);
        tv.setPadding(16, 16, 16, 16);
        
        
        // Phân biệt tin nhắn người dùng và tin nhắn bot
        if (msg.startsWith("Bạn: ")) {
            tv.setBackgroundResource(R.drawable.user_message_background);
        } else {
            tv.setBackgroundResource(R.drawable.bot_message_background);
        }
        
        chatLayout.addView(tv);
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
      /**
     * Check API availability and notify user if in offline mode
     */
    private void checkApiAvailability() {
        if (getContext() == null) return;
        
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(),
                    "Không có kết nối mạng. Đang sử dụng dữ liệu cục bộ.",
                    Toast.LENGTH_LONG).show();
            
            addMessage("Heritage Assistant: Tôi đang hoạt động trong chế độ ngoại tuyến với dữ liệu giới hạn. " +
                    "Một số thông tin có thể không được cập nhật.");
            return;
        }
        
        // Kiểm tra kết nối đến Heritage API
        chatService.checkApiConnection(new ChatService.ApiConnectionCallback() {
            @Override
            public void onConnectionResult(boolean isConnected, String message, String testedUrl) {
                if (getActivity() == null) return;
                
                getActivity().runOnUiThread(() -> {
                    if (!isConnected) {
                        Toast.makeText(getContext(),
                                "Không thể kết nối đến server Heritage: " + testedUrl,
                                Toast.LENGTH_LONG).show();
                        
                        Log.e(TAG, "Không thể kết nối đến API: " + testedUrl + ", Lỗi: " + message);
                        
                        addMessage("Heritage Assistant: Tôi đang gặp sự cố kết nối đến máy chủ dữ liệu di tích. " +
                                "Tôi sẽ sử dụng dữ liệu cục bộ.");
                    } else {
                        Log.d(TAG, "Kết nối thành công đến API: " + testedUrl);
                    }
                });
            }
        });
        
        // Check Gemini AI availability
        chatService.checkGeminiAvailability(getContext(), isAvailable -> {
            if (getActivity() == null) return;
            
            if (!isAvailable) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Không thể kết nối đến AI Assistant. Một số tính năng có thể bị giới hạn.",
                            Toast.LENGTH_LONG).show();
                    
                    addMessage("Heritage Assistant: Tôi đang gặp sự cố kết nối đến máy chủ AI. " +
                            "Tôi sẽ cố gắng cung cấp thông tin tốt nhất từ dữ liệu cục bộ.");
                });
            }
        });
    }
}
