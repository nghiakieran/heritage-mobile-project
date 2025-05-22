package hcmute.edu.vn.heritageproject.repository;

import java.util.ArrayList;
import java.util.List;
import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.models.PopularMonument;

public class MonumentRepository {
    public List<PopularMonument> getPopularMonuments() {
        List<PopularMonument> monuments = new ArrayList<>();
        // Dữ liệu giả lập (sẽ thay bằng gọi API sau)
        monuments.add(new PopularMonument("Hồ Hoàn Kiếm", "Quận Hoàn Kiếm, thủ đô Hà Nội", R.drawable.monument1));
        monuments.add(new PopularMonument("Chùa Một Cột", "Thành phố Hà Nội", R.drawable.monument2));
        monuments.add(new PopularMonument("Cổng Trùng Khánh", "Cầu Tràng Tiền, Huế", R.drawable.monument3));
        return monuments;
    }
}
