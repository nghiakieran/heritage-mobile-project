package hcmute.edu.vn.heritageproject.services;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import hcmute.edu.vn.heritageproject.models.Favorites;
import hcmute.edu.vn.heritageproject.repository.FirebaseFavoritesRepository;
import java.util.List;

public class FavoritesService {
    private static final String TAG = "FavoritesService";
    private static FavoritesService instance;
    private final FirebaseFavoritesRepository favoritesRepository;
    private final FirebaseAuth auth;

    private FavoritesService() {
        this.favoritesRepository = FirebaseFavoritesRepository.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public static synchronized FavoritesService getInstance() {
        if (instance == null) {
            instance = new FavoritesService();
        }
        return instance;
    }

    public interface FavoritesServiceCallback {
        void onSuccess(Favorites favorites);

        void onError(String error);
    }

    public interface FavoritesListServiceCallback {
        void onSuccess(List<Favorites> favoritesList);

        void onError(String error);
    }

    public void getCurrentUserFavorites(FavoritesServiceCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        favoritesRepository.getFavoritesByUserId(userId, new FirebaseFavoritesRepository.FavoritesCallback() {
            @Override
            public void onSuccess(Favorites favorites) {
                callback.onSuccess(favorites);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void addHeritageToFavorites(String heritageId, FavoritesServiceCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        favoritesRepository.addHeritageToFavorites(userId, heritageId,
                new FirebaseFavoritesRepository.FavoritesCallback() {
                    @Override
                    public void onSuccess(Favorites favorites) {
                        callback.onSuccess(favorites);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
    }

    public void removeHeritageFromFavorites(String heritageId, FavoritesServiceCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        favoritesRepository.removeHeritageFromFavorites(userId, heritageId,
                new FirebaseFavoritesRepository.FavoritesCallback() {
                    @Override
                    public void onSuccess(Favorites favorites) {
                        callback.onSuccess(favorites);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
    }

    public void getAllFavorites(FavoritesListServiceCallback callback) {
        favoritesRepository.getAllFavorites(new FirebaseFavoritesRepository.FavoritesListCallback() {
            @Override
            public void onSuccess(List<Favorites> favoritesList) {
                callback.onSuccess(favoritesList);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}