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

    private RecyclerView recyclerViewHeritages;
    private HeritageAdapter heritageAdapter;
    private List<Heritage> heritageList = new ArrayList<>();
    private List<Heritage> originalHeritageList = new ArrayList<>();

    private HeritageApiService apiService;
    private ProgressBar progressBar;
    private EditText searchEditText;
    private LinearLayout emptyContentLayout;
    private TextView emptyTextView;

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
        emptyTextView = view.findViewById(R.id.emptyTextView); // Thêm TextView trong layout nếu chưa có

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
        });

        apiService = HeritageApiService.getInstance();
        if (apiService == null) {
            Toast.makeText(getContext(), "Lỗi khởi tạo API service", Toast.LENGTH_SHORT).show();
            return;
        }        showEmptyState("Đang tải dữ liệu...");
        
        // Kiểm tra xem có từ khóa tìm kiếm được truyền từ HomeFragment không
        Bundle args = getArguments();
        if (args != null && args.containsKey("SEARCH_QUERY")) {
            String searchQuery = args.getString("SEARCH_QUERY");
            searchEditText.setText(searchQuery);
            // Khi đặt text, TextWatcher sẽ tự động kích hoạt tìm kiếm
        }
        
        loadAllHeritages();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                filterHeritages(query);
            }
        });
    }

    private void loadAllHeritages() {
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
                    if (result.getHeritages() != null && !result.getHeritages().isEmpty()) {
                        originalHeritageList.clear();
                        originalHeritageList.addAll(result.getHeritages());

                        heritageList.clear();
                        heritageList.addAll(originalHeritageList);
                        heritageAdapter.notifyDataSetChanged();
                        showHeritageList();
                    } else {
                        showEmptyState("Không có di tích nào.");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null || !isAdded()) return;

                getActivity().runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Lỗi khi tải danh sách di tích: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    showEmptyState("Không thể tải dữ liệu.");
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
}
