package hcmute.edu.vn.heritageproject.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import hcmute.edu.vn.heritageproject.api.HeritageApiService;
import hcmute.edu.vn.heritageproject.api.models.Heritage;
import hcmute.edu.vn.heritageproject.api.models.HeritageResponse;

public class HeritageRepository {
    private final HeritageApiService apiService;

    public HeritageRepository() {
        this.apiService = HeritageApiService.getInstance();
    }

    public interface HeritageCallback {
        void onHeritagesLoaded(List<Heritage> heritages);
        void onError(Exception e);
    }

    /**
     * Get the 5 most popular heritage sites based on the totalFavorites value
     */
    public void getPopularHeritages(final HeritageCallback callback) {
        apiService.getAllHeritages(new HeritageApiService.ApiCallback<HeritageResponse>() {
            @Override
            public void onSuccess(HeritageResponse result) {
                if (result != null && result.getHeritages() != null) {
                    List<Heritage> heritages = new ArrayList<>(result.getHeritages());
                    
                    // Sort by totalFavorites (descending)
                    Collections.sort(heritages, new Comparator<Heritage>() {
                        @Override
                        public int compare(Heritage h1, Heritage h2) {
                            int fav1 = 0, fav2 = 0;
                            
                            try {
                                if (h1.getStats() != null && h1.getStats().getTotalFavorites() != null) {
                                    fav1 = Integer.parseInt(h1.getStats().getTotalFavorites());
                                }
                            } catch (NumberFormatException e) {
                                fav1 = 0;
                            }
                            
                            try {
                                if (h2.getStats() != null && h2.getStats().getTotalFavorites() != null) {
                                    fav2 = Integer.parseInt(h2.getStats().getTotalFavorites());
                                }
                            } catch (NumberFormatException e) {
                                fav2 = 0;
                            }
                            
                            // Descending order
                            return Integer.compare(fav2, fav1);
                        }
                    });

                    // Take only the top 5 or less if fewer exist
                    int count = Math.min(heritages.size(), 5);
                    List<Heritage> popularHeritages = heritages.subList(0, count);
                    
                    callback.onHeritagesLoaded(popularHeritages);
                } else {
                    callback.onError(new Exception("No heritages found in response"));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Get 5 random heritage sites
     */
    public void getRandomHeritages(final HeritageCallback callback) {
        apiService.getAllHeritages(new HeritageApiService.ApiCallback<HeritageResponse>() {
            @Override
            public void onSuccess(HeritageResponse result) {
                if (result != null && result.getHeritages() != null && !result.getHeritages().isEmpty()) {
                    List<Heritage> heritages = new ArrayList<>(result.getHeritages());
                    
                    // Shuffle the list to get random elements
                    Collections.shuffle(heritages, new Random());
                    
                    // Take only the first 5 or less if fewer exist
                    int count = Math.min(heritages.size(), 5);
                    List<Heritage> randomHeritages = heritages.subList(0, count);
                    
                    callback.onHeritagesLoaded(randomHeritages);
                } else {
                    callback.onError(new Exception("No heritages found in response"));
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
