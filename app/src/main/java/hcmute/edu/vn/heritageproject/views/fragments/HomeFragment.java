package hcmute.edu.vn.heritageproject.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.models.PopularMonument;
import hcmute.edu.vn.heritageproject.repository.MonumentRepository;
import hcmute.edu.vn.heritageproject.views.adapters.PopularMonumentAdapter;
import hcmute.edu.vn.heritageproject.views.adapters.BannerAdapter;
import hcmute.edu.vn.heritageproject.views.adapters.EventAdapter;
import hcmute.edu.vn.heritageproject.models.Banner;
import hcmute.edu.vn.heritageproject.models.CulturalEvent;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewBanners;
    private RecyclerView recyclerViewPopularMonuments;
    private RecyclerView recyclerViewEvents;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup banner recycler view
        recyclerViewBanners = view.findViewById(R.id.recyclerViewBanners);
        recyclerViewBanners.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        // Setup popular monuments recycler view
        recyclerViewPopularMonuments = view.findViewById(R.id.recyclerViewPopularMonuments);
        recyclerViewPopularMonuments.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        // Setup events recycler view
        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Load data
        loadBanners();
        loadPopularMonuments();
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

    private void loadPopularMonuments() {
        MonumentRepository repository = new MonumentRepository();
        List<PopularMonument> monuments = repository.getPopularMonuments();
        PopularMonumentAdapter adapter = new PopularMonumentAdapter(monuments);
        recyclerViewPopularMonuments.setAdapter(adapter);
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
