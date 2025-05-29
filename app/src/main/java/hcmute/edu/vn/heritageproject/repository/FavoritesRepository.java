package hcmute.edu.vn.heritageproject.repository;

import hcmute.edu.vn.heritageproject.models.Favorites;
import java.util.List;

public interface FavoritesRepository {
    interface FavoritesCallback {
        void onSuccess(Favorites favorites);

        void onError(String error);
    }

    interface FavoritesListCallback {
        void onSuccess(List<Favorites> favoritesList);

        void onError(String error);
    }

    void createFavorites(String userId, FavoritesCallback callback);

    void getFavoritesByUserId(String userId, FavoritesCallback callback);

    void addHeritageToFavorites(String userId, String heritageId, FavoritesCallback callback);

    void removeHeritageFromFavorites(String userId, String heritageId, FavoritesCallback callback);

    void getAllFavorites(FavoritesListCallback callback);
}