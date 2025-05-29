package hcmute.edu.vn.heritageproject.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.repository.HeritageRepository;
import hcmute.edu.vn.heritageproject.views.adapters.HeritageAdapter;
import hcmute.edu.vn.heritageproject.views.adapters.BannerAdapter;
import hcmute.edu.vn.heritageproject.views.adapters.EventAdapter;
import hcmute.edu.vn.heritageproject.models.Banner;
import hcmute.edu.vn.heritageproject.models.CulturalEvent;
import hcmute.edu.vn.heritageproject.api.models.AppCache;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerViewBanners;
    private RecyclerView recyclerViewPopularHeritages;
    private RecyclerView recyclerViewRandomHeritages;
    private RecyclerView recyclerViewEvents;
    
    private HeritageRepository heritageRepository;
    private List<Heritage> popularHeritages = new ArrayList<>();
    private List<Heritage> randomHeritages = new ArrayList<>();
    private HeritageAdapter popularHeritageAdapter;
    private HeritageAdapter randomHeritageAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize repository
        heritageRepository = new HeritageRepository();
        
        // Setup banner recycler view (for featured heritages - most popular by totalFavorites)
        recyclerViewBanners = view.findViewById(R.id.recyclerViewBanners);
        recyclerViewBanners.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularHeritageAdapter = new HeritageAdapter(popularHeritages);
        recyclerViewBanners.setAdapter(popularHeritageAdapter);
        
        // Setup popular monuments recycler view (for random heritages)
        recyclerViewPopularHeritages = view.findViewById(R.id.recyclerViewPopularMonuments);
        recyclerViewPopularHeritages.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        randomHeritageAdapter = new HeritageAdapter(randomHeritages);
        recyclerViewPopularHeritages.setAdapter(randomHeritageAdapter);
        
        // Setup events recycler view
        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Load data
        loadFeaturedHeritages();
        loadPopularHeritages();
        loadEvents();

        return view;
    }

    private void loadBanners() {
        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner("Quảng bá Cố đô Huế", "Ghé thăm Đại Nội và Lăng Tẩm Nguyễn", R.drawable.banner_hue));
        banners.add(new Banner("Khám phá Hội An", "Thành phố cổ di sản UNESCO", R.drawable.banner_hoian));
        banners.add(new Banner("Thành nhà Hồ", "Di sản văn hóa thế giới tại Thanh Hóa", R.drawable.banner_thanhnhaho));
        
        BannerAdapter bannerAdapter = new BannerAdapter(banners);
        recyclerViewBanners.setAdapter(bannerAdapter);
    }    private void loadFeaturedHeritages() {
        // Dùng cache chung nếu có
        List<Heritage> cachedHeritages = AppCache.getHeritageList();
        if (cachedHeritages != null && !cachedHeritages.isEmpty()) {
            // Lấy top 5 di tích nổi bật (theo totalFavorites)
            List<Heritage> sorted = new ArrayList<>(cachedHeritages);
            sorted.sort((h1, h2) -> {
                int fav1 = 0, fav2 = 0;
                try { if (h1.getStats() != null && h1.getStats().getTotalFavorites() != null) fav1 = Integer.parseInt(h1.getStats().getTotalFavorites()); } catch (Exception ignored) {}
                try { if (h2.getStats() != null && h2.getStats().getTotalFavorites() != null) fav2 = Integer.parseInt(h2.getStats().getTotalFavorites()); } catch (Exception ignored) {}
                return Integer.compare(fav2, fav1);
            });
            int count = Math.min(sorted.size(), 5);
            popularHeritages.clear();
            popularHeritages.addAll(sorted.subList(0, count));
            popularHeritageAdapter.notifyDataSetChanged();
            return;
        }
        // Nếu chưa có cache thì gọi API như cũ
        heritageRepository.getPopularHeritages(new HeritageRepository.HeritageCallback() {
            @Override
            public void onHeritagesLoaded(final List<Heritage> heritages) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) {
                                popularHeritages.clear();
                                popularHeritages.addAll(heritages);
                                popularHeritageAdapter.notifyDataSetChanged();
                                // Cập nhật cache chung nếu chưa có
                                if (AppCache.getHeritageList() == null || AppCache.getHeritageList().isEmpty()) {
                                    AppCache.setHeritageList(new ArrayList<>(heritages));
                                }
                            }
                        }
                    });
                }
            }
            @Override
            public void onError(final Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) {
                                Log.e(TAG, "Failed to load popular heritages", e);
                                Toast.makeText(getContext(), "Không thể tải di tích nổi bật", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
    private void loadPopularHeritages() {
        // Dùng cache chung nếu có
        List<Heritage> cachedHeritages = AppCache.getHeritageList();
        if (cachedHeritages != null && !cachedHeritages.isEmpty()) {
            // Lấy ngẫu nhiên 5 di tích
            List<Heritage> shuffled = new ArrayList<>(cachedHeritages);
            java.util.Collections.shuffle(shuffled, new java.util.Random());
            int count = Math.min(shuffled.size(), 5);
            randomHeritages.clear();
            randomHeritages.addAll(shuffled.subList(0, count));
            randomHeritageAdapter.notifyDataSetChanged();
            // Set click listener cho các item
            randomHeritageAdapter.setOnHeritageClickListener(new HeritageAdapter.OnHeritageClickListener() {
                @Override
                public void onHeritageClick(Heritage heritage) {
                    Toast.makeText(getContext(), "Selected: " + heritage.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        // Nếu chưa có cache thì gọi API như cũ
        heritageRepository.getRandomHeritages(new HeritageRepository.HeritageCallback() {
            @Override
            public void onHeritagesLoaded(final List<Heritage> heritages) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) {
                                randomHeritages.clear();
                                randomHeritages.addAll(heritages);
                                randomHeritageAdapter.notifyDataSetChanged();
                                randomHeritageAdapter.setOnHeritageClickListener(new HeritageAdapter.OnHeritageClickListener() {
                                    @Override
                                    public void onHeritageClick(Heritage heritage) {
                                        Toast.makeText(getContext(), "Selected: " + heritage.getName(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                // Cập nhật cache chung nếu chưa có
                                if (AppCache.getHeritageList() == null || AppCache.getHeritageList().isEmpty()) {
                                    AppCache.setHeritageList(new ArrayList<>(heritages));
                                }
                            }
                        }
                    });
                }
            }
            @Override
            public void onError(final Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) {
                                Log.e(TAG, "Failed to load random heritages", e);
                                Toast.makeText(getContext(), "Không thể tải di tích phổ biến", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void loadEvents() {
        List<CulturalEvent> events = new ArrayList<>();
        events.add(new CulturalEvent("Lễ hội Đền Hùng", 
            "03/06/2025 - 10/06/2025", 
            "Phú Thọ", 
            "Lễ hội tưởng nhớ các Vua Hùng đã có công dựng nước", 
            R.drawable.event_den_hung));
        events.add(new CulturalEvent("Festival Huế 2025", 
            "01/07/2025 - 06/07/2025", 
            "Thừa Thiên Huế", 
            "Lễ hội văn hóa, nghệ thuật quốc tế diễn ra 2 năm một lần", 
            R.drawable.event_hue));
        events.add(new CulturalEvent("Hội Đèn Lồng Hội An", 
            "14/08/2025", 
            "Quảng Nam", 
            "Đêm phố cổ lung linh ánh đèn lồng vào đêm rằm", 
            R.drawable.event_hoian));
        
        EventAdapter eventAdapter = new EventAdapter(events);
        recyclerViewEvents.setAdapter(eventAdapter);
    }
}
