package hcmute.edu.vn.heritageproject.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;

/**
 * Local data source for heritage information.
 * Used as fallback when API is unavailable.
 */
public class LocalHeritageDataSource {
    
    /**
     * Get a fallback heritage response with some predefined data
     */
    public static HeritageResponse getFallbackHeritageData() {
        HeritageResponse response = new HeritageResponse();
        response.setSuccess(true);
        response.setMessage("Local data loaded successfully");
        response.setHeritages(getDefaultHeritageList());
        return response;
    }
    
    /**
     * Get a default list of heritage sites
     */
    private static List<Heritage> getDefaultHeritageList() {
        List<Heritage> heritages = new ArrayList<>();
        
        // Heritage 1: Hội An Ancient Town
        Heritage heritage1 = new Heritage();
        heritage1.setId("hoian");
        heritage1.setName("Phố cổ Hội An");
        heritage1.setDescription("Phố cổ Hội An là một đô thị cổ nằm ở hạ lưu sông Thu Bồn, " +
                "thuộc vùng đồng bằng ven biển tỉnh Quảng Nam, Việt Nam. " +
                "Đây là một điển hình của một cảng thị cổ ở Đông Nam Á từ thế kỷ 15 đến thế kỷ 19.");
        heritage1.setLocation("Quảng Nam");
        heritage1.setLocationSlug("quang-nam");
        heritage1.setImages(Arrays.asList("hoian1.jpg", "hoian2.jpg"));
        heritages.add(heritage1);
        
        // Heritage 2: Ha Long Bay
        Heritage heritage2 = new Heritage();
        heritage2.setId("halong");
        heritage2.setName("Vịnh Hạ Long");
        heritage2.setDescription("Vịnh Hạ Long là một vịnh nhỏ thuộc phần bờ tây vịnh Bắc Bộ, " +
                "bao gồm vùng biển đảo thuộc thành phố Hạ Long, thị xã Cẩm Phả và một phần huyện đảo Vân Đồn, tỉnh Quảng Ninh.");
        heritage2.setLocation("Quảng Ninh");
        heritage2.setLocationSlug("quang-ninh");
        heritage2.setImages(Arrays.asList("halong1.jpg", "halong2.jpg"));
        heritages.add(heritage2);
        
        // Heritage 3: Huong Pagoda
        Heritage heritage3 = new Heritage();
        heritage3.setId("huongpagoda");
        heritage3.setName("Chùa Hương");
        heritage3.setDescription("Chùa Hương hay còn gọi là Hương Sơn, là một quần thể di tích thắng cảnh " +
                "gồm cả danh lam thắng cảnh và di tích lịch sử, văn hóa tại xã Hương Sơn, huyện Mỹ Đức, thành phố Hà Nội.");
        heritage3.setLocation("Hà Nội");
        heritage3.setLocationSlug("ha-noi");
        heritage3.setImages(Arrays.asList("huong1.jpg", "huong2.jpg"));
        heritages.add(heritage3);
        
        // Heritage 4: My Son Sanctuary
        Heritage heritage4 = new Heritage();
        heritage4.setId("myson");
        heritage4.setName("Thánh địa Mỹ Sơn");
        heritage4.setDescription("Thánh địa Mỹ Sơn (tiếng Chăm: Bimong Mỹ Sơn) là một khu đền đài mang tính " +
                "bảo tồn tôn giáo rộng lớn của vương quốc Chăm Pa tại Việt Nam.");
        heritage4.setLocation("Quảng Nam");
        heritage4.setLocationSlug("quang-nam");
        heritage4.setImages(Arrays.asList("myson1.jpg", "myson2.jpg"));
        heritages.add(heritage4);
        
        // Heritage 5: Trang An Complex
        Heritage heritage5 = new Heritage();
        heritage5.setId("trangan");
        heritage5.setName("Quần thể danh thắng Tràng An");
        heritage5.setDescription("Quần thể danh thắng Tràng An là một di sản thế giới được UNESCO công nhận " +
                "tại Việt Nam, bao gồm một quần thể danh thắng nằm ở vùng Tràng An, tỉnh Ninh Bình.");
        heritage5.setLocation("Ninh Bình");
        heritage5.setLocationSlug("ninh-binh");
        heritage5.setImages(Arrays.asList("trangan1.jpg", "trangan2.jpg"));
        heritages.add(heritage5);
        
        return heritages;
    }
    
    /**
     * Search for heritages in the local data that match the query
     */
    public static List<Heritage> searchLocalHeritages(String query) {
        List<Heritage> allHeritages = getDefaultHeritageList();
        List<Heritage> matchingHeritages = new ArrayList<>();
        
        if (query == null || query.isEmpty()) {
            return allHeritages;
        }
        
        String queryLower = query.toLowerCase();
        
        for (Heritage heritage : allHeritages) {
            if (heritage.getName().toLowerCase().contains(queryLower) ||
                    heritage.getDescription().toLowerCase().contains(queryLower) ||
                    heritage.getLocation().toLowerCase().contains(queryLower)) {
                matchingHeritages.add(heritage);
            }
        }
        
        return matchingHeritages;
    }
}
