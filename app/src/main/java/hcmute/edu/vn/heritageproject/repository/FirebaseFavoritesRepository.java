package hcmute.edu.vn.heritageproject.repository;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.Timestamp;
import hcmute.edu.vn.heritageproject.models.Favorites;
import java.util.ArrayList;
import java.util.List;

public class FirebaseFavoritesRepository implements FavoritesRepository {
    private static final String TAG = "FirebaseFavoritesRepository";
    private static FirebaseFavoritesRepository instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION_FAVORITES = "favorites";

    private FirebaseFavoritesRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseFavoritesRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseFavoritesRepository();
        }
        return instance;
    }

    @Override
    public void createFavorites(String userId, FavoritesCallback callback) {
        Favorites favorites = new Favorites();
        favorites.setId(userId); // Sử dụng userId làm id của favorites
        favorites.setUserId(userId);
        favorites.setItems(new ArrayList<>());
        favorites.setCreatedAt(Timestamp.now());
        favorites.setUpdatedAt(Timestamp.now());

        db.collection(COLLECTION_FAVORITES).document(userId)
                .set(favorites, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Favorites created successfully");
                    callback.onSuccess(favorites);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating favorites", e);
                    callback.onError("Lỗi khi tạo danh sách yêu thích: " + e.getMessage());
                });
    }

    @Override
    public void getFavoritesByUserId(String userId, FavoritesCallback callback) {
        db.collection(COLLECTION_FAVORITES).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Favorites favorites = documentSnapshot.toObject(Favorites.class);
                        if (favorites != null) {
                            callback.onSuccess(favorites);
                        } else {
                            callback.onError("Không thể chuyển đổi dữ liệu yêu thích");
                        }
                    } else {
                        // Nếu chưa có favorites, tạo mới
                        createFavorites(userId, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting favorites", e);
                    callback.onError("Lỗi khi lấy danh sách yêu thích: " + e.getMessage());
                });
    }

    @Override
    public void addHeritageToFavorites(String userId, String heritageId, FavoritesCallback callback) {
        Favorites.FavoriteItem newItem = new Favorites.FavoriteItem(heritageId);

        db.collection(COLLECTION_FAVORITES).document(userId)
                .update(
                        "items", com.google.firebase.firestore.FieldValue.arrayUnion(newItem),
                        "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Heritage added to favorites successfully");
                    getFavoritesByUserId(userId, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding heritage to favorites", e);
                    callback.onError("Lỗi khi thêm di tích vào danh sách yêu thích: " + e.getMessage());
                });
    }

    @Override
    public void removeHeritageFromFavorites(String userId, String heritageId, FavoritesCallback callback) {
        db.collection(COLLECTION_FAVORITES).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Favorites favorites = documentSnapshot.toObject(Favorites.class);
                        if (favorites != null && favorites.getItems() != null) {
                            List<Favorites.FavoriteItem> items = favorites.getItems();
                            items.removeIf(item -> item.getHeritageId().equals(heritageId));

                            db.collection(COLLECTION_FAVORITES).document(userId)
                                    .update(
                                            "items", items,
                                            "updatedAt", Timestamp.now())
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Heritage removed from favorites successfully");
                                        getFavoritesByUserId(userId, callback);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error removing heritage from favorites", e);
                                        callback.onError(
                                                "Lỗi khi xóa di tích khỏi danh sách yêu thích: " + e.getMessage());
                                    });
                        } else {
                            callback.onError("Không tìm thấy danh sách yêu thích");
                        }
                    } else {
                        callback.onError("Không tìm thấy danh sách yêu thích");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting favorites for removal", e);
                    callback.onError("Lỗi khi lấy danh sách yêu thích: " + e.getMessage());
                });
    }

    @Override
    public void getAllFavorites(FavoritesListCallback callback) {
        db.collection(COLLECTION_FAVORITES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Favorites> favoritesList = new ArrayList<>();
                    queryDocumentSnapshots.forEach(doc -> {
                        Favorites favorites = doc.toObject(Favorites.class);
                        if (favorites != null) {
                            favoritesList.add(favorites);
                        }
                    });
                    callback.onSuccess(favoritesList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all favorites", e);
                    callback.onError("Lỗi khi lấy danh sách yêu thích: " + e.getMessage());
                });
    }
}