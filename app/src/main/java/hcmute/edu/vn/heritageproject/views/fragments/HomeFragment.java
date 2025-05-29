package hcmute.edu.vn.heritageproject.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide; // Import Glide

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.repository.HeritageRepository;
import hcmute.edu.vn.heritageproject.views.adapters.HeritageAdapter;
import hcmute.edu.vn.heritageproject.views.adapters.BannerAdapter;
import hcmute.edu.vn.heritageproject.views.adapters.EventAdapter;
import hcmute.edu.vn.heritageproject.views.adapters.HeroCarouselAdapter;
import hcmute.edu.vn.heritageproject.models.Banner;
import hcmute.edu.vn.heritageproject.models.CulturalEvent;
import hcmute.edu.vn.heritageproject.models.HeroSlide;
import hcmute.edu.vn.heritageproject.views.HeritageDetailActivity;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerViewBanners;
    private RecyclerView recyclerViewPopularHeritages;
    private RecyclerView recyclerViewRandomHeritages;
    private RecyclerView recyclerViewEvents;
    private ProgressBar loadingProgressBar;
    private View scrollContent;
    private EditText searchEditText;
    private ViewPager2 heroCarousel;
    private ImageView gifImageView; // Declare ImageView for the GIF

    private HeritageRepository heritageRepository;
    private List<Heritage> popularHeritages = new ArrayList<>();
    private List<Heritage> randomHeritages = new ArrayList<>();
    private HeritageAdapter popularHeritageAdapter;
    private HeritageAdapter randomHeritageAdapter;

    private List<HeroSlide> heroSlides = new ArrayList<>();
    private HeroCarouselAdapter heroCarouselAdapter;

    // Variables to track loading state
    private int pendingApiCalls = 0;
    private final Object apiCallLock = new Object();

    private Runnable heroCarouselRunnable;
    private int heroCarouselCurrentPosition = 0;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize repository
        heritageRepository = new HeritageRepository();

        // Initialize loading indicator and content view
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        scrollContent = view.findViewById(R.id.scrollContent);
        showLoading(true);        // Initialize search EditText and Button
        searchEditText = view.findViewById(R.id.searchEditText);
        ImageView searchButton = view.findViewById(R.id.searchButton);
        setupSearchFunctionality();
        
        // Thêm sự kiện cho nút tìm kiếm
        searchButton.setOnClickListener(v -> performSearch());

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

        // Setup hero carousel (ViewPager2)
        heroCarousel = view.findViewById(R.id.heroCarousel);
        setupHeroCarousel();

        // Initialize and load GIF using Glide
        gifImageView = view.findViewById(R.id.gifImageView); // Find the ImageView by its ID
        if (gifImageView != null) {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.video) // Make sure heritage_animation.gif is in drawable folder
                    .into(gifImageView);
        } else {
            Log.e(TAG, "gifImageView not found in layout.");
        }

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
    }

    private void loadFeaturedHeritages() {
        // Get popular heritages (sorted by totalFavorites)
        incrementPendingApiCall(); // Mark that we're starting an API call
        heritageRepository.getPopularHeritages(new HeritageRepository.HeritageCallback() {
            @Override
            public void onHeritagesLoaded(final List<Heritage> heritages) {
                // Chuyển cập nhật UI về main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) {  // Kiểm tra Fragment còn được đính kèm vào Activity không
                                popularHeritages.clear();
                                popularHeritages.addAll(heritages);
                                popularHeritageAdapter.notifyDataSetChanged();

                                // Set click listener for heritage items
                                popularHeritageAdapter.setOnHeritageClickListener(new HeritageAdapter.OnHeritageClickListener() {
                                    @Override
                                    public void onHeritageClick(Heritage heritage) {
                                        Intent intent = new Intent(getContext(), HeritageDetailActivity.class);
                                        intent.putExtra("heritageId", heritage.getId());
                                        startActivity(intent);
                                    }                                });

                                decrementPendingApiCall();
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
                            if (isAdded()) {  // Kiểm tra Fragment còn được đính kèm vào Activity không
                                Log.e(TAG, "Failed to load popular heritages", e);
                                Toast.makeText(getContext(), "Không thể tải di tích nổi bật", Toast.LENGTH_SHORT).show();

                                // Mark this API call as complete even though it failed
                                decrementPendingApiCall();
                            }
                        }
                    });
                }
            }
        });
    }

    private void loadPopularHeritages() {
        // Get random heritages
        incrementPendingApiCall(); // Mark that we're starting an API call
        heritageRepository.getRandomHeritages(new HeritageRepository.HeritageCallback() {
            @Override
            public void onHeritagesLoaded(final List<Heritage> heritages) {
                // Chuyển cập nhật UI về main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) {  // Kiểm tra Fragment còn được đính kèm vào Activity không
                                randomHeritages.clear();
                                randomHeritages.addAll(heritages);
                                randomHeritageAdapter.notifyDataSetChanged();

                                // Set click listener for heritage items
                                randomHeritageAdapter.setOnHeritageClickListener(new HeritageAdapter.OnHeritageClickListener() {
                                    @Override
                                    public void onHeritageClick(Heritage heritage) {
                                        Intent intent = new Intent(getContext(), HeritageDetailActivity.class);
                                        intent.putExtra("heritageId", heritage.getId());
                                        startActivity(intent);
                                    }
                                });

                                // Mark this API call as complete
                                decrementPendingApiCall();
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
                            if (isAdded()) {  // Kiểm tra Fragment còn được đính kèm vào Activity không
                                Log.e(TAG, "Failed to load random heritages", e);
                                Toast.makeText(getContext(), "Không thể tải di tích phổ biến", Toast.LENGTH_SHORT).show();

                                // Mark this API call as complete even though it failed
                                decrementPendingApiCall();
                            }
                        }
                    });
                }
            }
        });
    }

    private void loadEvents() {
        // Increment the API call counter to reflect that we're loading data
        incrementPendingApiCall();

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

        // Mark the events loading as complete (since it's using local data)
        decrementPendingApiCall();
    }

    private void showLoading(final boolean isLoading) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        // Show loading indicator and hide content when loading
                        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                        scrollContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
                    }
                }
            });
        }
    }

    private void incrementPendingApiCall() {
        synchronized (apiCallLock) {
            pendingApiCalls++;
            showLoading(true);
        }
    }

    private void decrementPendingApiCall() {
        synchronized (apiCallLock) {
            pendingApiCalls--;
            if (pendingApiCalls <= 0) {
                pendingApiCalls = 0; // Ensure it doesn't go negative
                showLoading(false);
            }
        }
    }    private void setupSearchFunctionality() {
        // Xử lý khi người dùng nhấn nút tìm kiếm trên bàn phím
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }
    
    private void performSearch() {
        String searchQuery = searchEditText.getText().toString().trim();
        if (!searchQuery.isEmpty()) {
            
            // Ẩn bàn phím
            hideSoftKeyboard();
            
            // Chuyển sang HeritageFragment với từ khóa tìm kiếm
            navigateToHeritageFragmentWithSearch(searchQuery);
        } else {
            // Hiển thị thông báo nếu người dùng chưa nhập từ khóa
            Toast.makeText(getContext(), "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void hideSoftKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            searchEditText.clearFocus();
        }
    }

    private void navigateToHeritageFragmentWithSearch(String searchQuery) {
        if (getActivity() != null) {
            // Tạo một instance mới của HeritageFragment với từ khóa tìm kiếm
            HeritageFragment heritageFragment = new HeritageFragment();
            Bundle args = new Bundle();
            args.putString("SEARCH_QUERY", searchQuery);
            heritageFragment.setArguments(args);

            // Chuyển đến HeritageFragment
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, heritageFragment)
                    .addToBackStack(null)
                    .commit();

            // Hiển thị thông báo đang chuyển hướng tìm kiếm
            // Toast.makeText(getContext(), "Đang tìm kiếm: " + searchQuery, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupHeroCarousel() {
        heroSlides.clear();
        heroSlides.add(new HeroSlide(1,
                "https://images.unsplash.com/photo-1742156345582-b857d994c84e?q=50&w=1200&auto=format&fit=crop&ixlib=rb-4.0.3",
                "Khám phá Di sản Văn hóa Việt Nam",
                "Hành trình qua hàng thế kỷ lịch sử, văn hóa và thiên nhiên kỳ vĩ."));
        heroSlides.add(new HeroSlide(2,
                "https://images.unsplash.com/photo-1741812191037-96bb5f12010a?q=50&w=1200&auto=format&fit=crop&ixlib=rb-4.0.3",
                "Ngược dòng thời gian",
                "Trải nghiệm những di tích lịch sử và văn hóa quan trọng nhất."));
        heroSlides.add(new HeroSlide(3,
                "https://images.unsplash.com/photo-1741851374411-9528e6d2f33f?q=50&w=1200&auto=format&fit=crop&ixlib=rb-4.0.3",
                "Kết nối với quá khứ Việt Nam",
                "Đắm chìm trong những câu chuyện đã định hình nên dân tộc ta."));

        heroCarouselAdapter = new HeroCarouselAdapter(getContext(), heroSlides);
        heroCarousel.setAdapter(heroCarouselAdapter);
        heroCarousel.setOffscreenPageLimit(1);

        heroCarouselRunnable = new Runnable() {
            @Override
            public void run() {
                if (heroSlides.size() == 0 || heroCarousel == null) return;
                heroCarouselCurrentPosition = heroCarousel.getCurrentItem();
                int nextPos = (heroCarouselCurrentPosition + 1) % heroSlides.size();
                heroCarousel.setCurrentItem(nextPos, true);
                heroCarousel.postDelayed(this, 2000);
            }
        };
        heroCarousel.postDelayed(heroCarouselRunnable, 2000);
    }

    @Override
    public void onDestroyView() {
        if (heroCarousel != null && heroCarouselRunnable != null) {
            heroCarousel.removeCallbacks(heroCarouselRunnable);
        }
        super.onDestroyView();
    }
}