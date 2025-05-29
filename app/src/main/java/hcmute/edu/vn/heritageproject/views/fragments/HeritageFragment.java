package hcmute.edu.vn.heritageproject.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.api.HeritageApiService;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;
import hcmute.edu.vn.heritageproject.utils.NetworkUtils;
import hcmute.edu.vn.heritageproject.views.HeritageDetailActivity;
import hcmute.edu.vn.heritageproject.views.adapters.HeritageAdapter;

public class HeritageFragment extends Fragment {

    private static final String TAG = "HeritageFragment";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 50;

    // Cache tĩnh - chỉ cần thêm dòng này
    private static List<Heritage> cachedHeritages = null;

    private RecyclerView recyclerViewHeritages;
    private HeritageAdapter heritageAdapter;
    private List<Heritage> heritageList = new ArrayList<>();
    private List<Heritage> originalHeritageList = new ArrayList<>();

    private HeritageApiService apiService;
    private ProgressBar progressBar;
    private EditText searchEditText;
    private LinearLayout emptyContentLayout;
    private TextView emptyTextView;
    private SwipeRefreshLayout swipeRefreshLayout; // Thêm SwipeRefreshLayout

    public HeritageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_heritage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewHeritages = view.findViewById(R.id.recyclerViewHeritages);
        progressBar = view.findViewById(R.id.progressBar);
        searchEditText = view.findViewById(R.id.searchEditText);
        emptyContentLayout = view.findViewById(R.id.emptyContentLayout);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout); // Nếu có trong layout

        // Sử dụng GridLayoutManager với 2 cột
        recyclerViewHeritages.setLayoutManager(new GridLayoutManager(getContext(), 2));
        heritageAdapter = new HeritageAdapter(heritageList);
        recyclerViewHeritages.setAdapter(heritageAdapter);

        heritageAdapter.setOnHeritageClickListener(heritage -> {
            if (heritage.getId() == null || heritage.getId().isEmpty()) {
                Toast.makeText(getContext(), "ID di tích không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getContext(), HeritageDetailActivity.class);
            intent.putExtra("heritageId", heritage.getId());
            startActivity(intent);
        });        apiService = HeritageApiService.getInstance();
        if (apiService == null) {
            Toast.makeText(getContext(), "Lỗi khởi tạo API service", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup SwipeRefreshLayout nếu có
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Xóa cache và gọi lại API                cachedHeritages = null;
                loadAllHeritages(null); // Pass null as search query to load all heritages
            });
        }

        showEmptyState("Đang tải dữ liệu...");
        
        // Lưu trữ từ khóa tìm kiếm để sử dụng sau khi dữ liệu được tải
        final String savedSearchQuery = getSavedSearchQuery();
        
        // Tải dữ liệu và thực hiện tìm kiếm sau khi dữ liệu đã sẵn sàng
        loadAllHeritages(savedSearchQuery);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                filterHeritages(query);
            }
        });
    }    private void loadAllHeritages(final String searchQuery) {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            showOfflineMessage();
            return;
        }

        showLoading();

        apiService.getAllHeritages(DEFAULT_PAGE, DEFAULT_LIMIT, null, new HeritageApiService.ApiCallback<HeritageResponse>() {
            @Override
            public void onSuccess(HeritageResponse result) {
                if (getActivity() == null || !isAdded()) return;

                getActivity().runOnUiThread(() -> {
                    hideLoading();
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    if (result.getHeritages() != null && !result.getHeritages().isEmpty()) {
                        // Lưu vào cache
                        cachedHeritages = new ArrayList<>(result.getHeritages());
                        
                        originalHeritageList.clear();
                        originalHeritageList.addAll(result.getHeritages());

                        heritageList.clear();
                        heritageList.addAll(originalHeritageList);
                        heritageAdapter.notifyDataSetChanged();
                        
                        // Nếu có từ khóa tìm kiếm, thực hiện tìm kiếm sau khi dữ liệu đã được tải
                        if (searchQuery != null && !searchQuery.isEmpty()) {
                            // Đặt text vào ô tìm kiếm nhưng tạm thời tắt TextWatcher
                            if (searchEditText.getTag() == null) {
                                TextWatcher savedTextWatcher = null;
                                for (TextWatcher watcher : getTextWatchersFrom(searchEditText)) {
                                    savedTextWatcher = watcher;
                                    searchEditText.removeTextChangedListener(watcher);
                                }
                                searchEditText.setText(searchQuery);
                                if (savedTextWatcher != null) {
                                    searchEditText.addTextChangedListener(savedTextWatcher);
                                }
                                // Thực hiện tìm kiếm thủ công
                                filterHeritages(searchQuery);
                            } else {
                                searchEditText.setText(searchQuery);
                            }
                            
                            
                        } else {
                            showHeritageList();
                        }
                    } else {
                        showEmptyState("Không có di tích nào.");
                    }
                });
            }            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;

                getActivity().runOnUiThread(() -> {
                    hideLoading();
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(getContext(), "Lỗi khi tải danh sách di tích: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showEmptyState("Không thể tải dữ liệu.");
                    
                    // Nếu có từ khóa tìm kiếm, vẫn hiển thị trong ô tìm kiếm
                    if (searchQuery != null && !searchQuery.isEmpty()) {
                        searchEditText.setText(searchQuery);
                        Toast.makeText(getContext(), "Không thể tìm kiếm do lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void filterHeritages(String query) {
        heritageList.clear();

        String normalizedQuery = normalize(query);

        for (Heritage h : originalHeritageList) {
            String name = h.getName() != null ? h.getName() : "";
            if (normalize(name).contains(normalizedQuery)) {
                heritageList.add(h);
            }
        }

        if (heritageList.isEmpty()) {
            showEmptyState("Không có di tích nào phù hợp.");
        } else {
            showHeritageList();
        }
    }

    private String normalize(String input) {
        if (input == null) return "";
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewHeritages.setVisibility(View.GONE);
        emptyContentLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showHeritageList() {
        recyclerViewHeritages.setVisibility(View.VISIBLE);
        emptyContentLayout.setVisibility(View.GONE);
        heritageAdapter.notifyDataSetChanged();
    }

    private void showEmptyState(String message) {
        recyclerViewHeritages.setVisibility(View.GONE);
        emptyContentLayout.setVisibility(View.VISIBLE);
        if (emptyTextView != null) {
            emptyTextView.setText(message);
        }
    }

    private void showOfflineMessage() {
        Toast.makeText(getContext(), "Không có kết nối mạng. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
        showEmptyState("Không có kết nối mạng.");
    }

    /**
     * Lấy từ khóa tìm kiếm được truyền từ HomeFragment (nếu có)
     * @return Từ khóa tìm kiếm hoặc null nếu không có
     */
    private String getSavedSearchQuery() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("SEARCH_QUERY")) {
            String searchQuery = args.getString("SEARCH_QUERY");
            if (searchQuery != null && !searchQuery.isEmpty()) {
                return searchQuery;
            }
        }
        return null;
    }

    /**
     * Phương thức tiện ích để lấy danh sách TextWatcher từ một EditText
     * @param editText EditText cần lấy TextWatcher
     * @return Danh sách các TextWatcher đã đăng ký
     */
    private List<TextWatcher> getTextWatchersFrom(EditText editText) {
        // Vì Android không cung cấp API để lấy danh sách TextWatcher,
        // chúng ta cần tự quản lý TextWatcher
        List<TextWatcher> watchers = new ArrayList<>();
        try {
            // Cố gắng lấy field mListeners từ EditText (chỉ hoạt động trên một số phiên bản Android)
            Field field = TextView.class.getDeclaredField("mListeners");
            field.setAccessible(true);
            ArrayList<?> listeners = (ArrayList<?>) field.get(editText);
            if (listeners != null) {
                for (Object listener : listeners) {
                    if (listener instanceof TextWatcher) {
                        watchers.add((TextWatcher) listener);
                    }
                }
            }
        } catch (Exception e) {
            // Không thể lấy TextWatcher qua reflection, trả về danh sách trống
            Log.e(TAG, "Không thể lấy TextWatcher: " + e.getMessage());
        }
        return watchers;
    }
}