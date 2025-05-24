package hcmute.edu.vn.heritageproject.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.widget.NestedScrollView;

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
    private NestedScrollView scrollView;
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
    }    private void showTypingIndicator() {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            // Tạo container cho indicator
            LinearLayout indicatorContainer = new LinearLayout(getContext());
            indicatorContainer.setOrientation(LinearLayout.VERTICAL);
            indicatorContainer.setId(R.id.typing_indicator);
            
            TextView typingIndicator = new TextView(getContext());
            typingIndicator.setText("Heritage Assistant đang nhập...");
            typingIndicator.setPadding(16, 16, 16, 16);
            typingIndicator.setTextColor(0xFF757575); // Màu chữ xám
            typingIndicator.setBackgroundResource(R.drawable.bot_message_background);
            
            // Thiết lập animation hiệu ứng nhấp nháy (có thể thêm sau)
            
            // Thiết lập layout params cho container
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            
            // Thêm margins
            int marginBottomInDp = 16;
            int marginTopInDp = 4;
            int marginLeftInDp = 8;
            
            int pxToBottom = (int) (marginBottomInDp * getResources().getDisplayMetrics().density);
            int pxToTop = (int) (marginTopInDp * getResources().getDisplayMetrics().density);
            int pxToLeft = (int) (marginLeftInDp * getResources().getDisplayMetrics().density);
            
            containerParams.bottomMargin = pxToBottom;
            containerParams.topMargin = pxToTop;
            containerParams.leftMargin = pxToLeft;
            containerParams.gravity = Gravity.START;
            containerParams.rightMargin = (int) (getResources().getDisplayMetrics().widthPixels * 0.4); // Giới hạn chiều rộng
            
            // Thêm TextView vào container
            indicatorContainer.addView(typingIndicator);
            
            // Set layout params cho container
            indicatorContainer.setLayoutParams(containerParams);
            
            // Thêm shadow nếu hỗ trợ
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                typingIndicator.setElevation(1f);
            }
            
            chatLayout.addView(indicatorContainer);
            scrollToBottom();
        });
    }
      private void removeTypingIndicator() {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            View indicator = chatLayout.findViewById(R.id.typing_indicator);
            if (indicator != null) {
                // Add fade out animation (optional)
                indicator.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> chatLayout.removeView(indicator))
                    .start();
            }
        });
    }private void addMessage(String msg) {
        // Tạo container cho tin nhắn để thêm shadow và styling
        LinearLayout messageContainer = new LinearLayout(getContext());
        messageContainer.setOrientation(LinearLayout.VERTICAL);
        
        TextView tv = new TextView(getContext());
        tv.setText(msg);
        tv.setPadding(16, 16, 16, 16);
        
        // Thiết lập margin để tạo khoảng cách giữa các tin nhắn
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        
        // Thêm margin phía dưới mỗi tin nhắn (đơn vị dp)
        int marginBottomInDp = 16;
        int marginTopInDp = 4;
        int horizontalMarginInDp = 8;
        int pxToBottom = (int) (marginBottomInDp * getResources().getDisplayMetrics().density);
        int pxToTop = (int) (marginTopInDp * getResources().getDisplayMetrics().density);
        int pxToSide = (int) (horizontalMarginInDp * getResources().getDisplayMetrics().density);
        
        containerParams.bottomMargin = pxToBottom;
        containerParams.topMargin = pxToTop;
        
        // Phân biệt tin nhắn người dùng và tin nhắn bot
        if (msg.startsWith("Bạn: ")) {
            tv.setBackgroundResource(R.drawable.user_message_background);
            tv.setTextColor(0xFF000000); // Màu chữ đen
            
            // Đẩy tin nhắn của người dùng về bên phải
            containerParams.gravity = Gravity.END;
            containerParams.rightMargin = pxToSide;
            containerParams.leftMargin = (int) (getResources().getDisplayMetrics().widthPixels * 0.15); // Giới hạn chiều rộng
            
            // Thêm shadow cho tin nhắn người dùng (Android 5.0+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                tv.setElevation(2f); 
            }
        } else {
            tv.setBackgroundResource(R.drawable.bot_message_background);
            tv.setTextColor(0xFF000000); // Màu chữ đen
            
            // Đẩy tin nhắn của bot về bên trái
            containerParams.gravity = Gravity.START;
            containerParams.leftMargin = pxToSide;
            containerParams.rightMargin = (int) (getResources().getDisplayMetrics().widthPixels * 0.15); // Giới hạn chiều rộng
            
            // Thêm shadow cho tin nhắn bot (Android 5.0+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                tv.setElevation(1f);
            }
        }
        
        // Thêm TextView vào container
        messageContainer.addView(tv);
        
        // Set layout params cho container
        messageContainer.setLayoutParams(containerParams);
        
        // Thêm message container vào chat layout
        chatLayout.addView(messageContainer);
        scrollToBottom();
    }
      private void scrollToBottom() {
        scrollView.post(() -> {
            // Đối với NestedScrollView, fullScroll vẫn hoạt động
            scrollView.fullScroll(View.FOCUS_DOWN);
            
            // Thêm smooth scroll animation
            if (chatLayout.getChildCount() > 0) {
                View lastChild = chatLayout.getChildAt(chatLayout.getChildCount() - 1);
                if (lastChild != null) {
                    lastChild.requestFocus();
                }
            }
        });
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
