package hcmute.edu.vn.heritageproject.repository;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.Timestamp;
import hcmute.edu.vn.heritageproject.models.User;
import java.util.ArrayList;

public class FirebaseUserRepository implements UserRepository {
    private static final String TAG = "FirebaseUserRepository";
    private static FirebaseUserRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private static final String COLLECTION_USERS = "users";

    private FirebaseUserRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseUserRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseUserRepository();
        }
        return instance;
    }

    @Override
    public void createUser(FirebaseUser firebaseUser, UserCallback callback) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setDisplayName(firebaseUser.getDisplayName());
        user.setPhotoUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
        user.setCreatedAt(Timestamp.now());
        user.setUpdatedAt(Timestamp.now());

        db.collection(COLLECTION_USERS).document(user.getId())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New user created successfully");
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating new user", e);
                    callback.onError("Lỗi khi tạo tài khoản mới: " + e.getMessage());
                });
    }

    @Override
    public void updateUser(User user, UserCallback callback) {
        if (user == null || user.getId() == null) {
            callback.onError("User or User ID is null");
            return;
        }

        user.setUpdatedAt(Timestamp.now());
        db.collection(COLLECTION_USERS).document(user.getId())
                .update(
                        "displayName", user.getDisplayName(),
                        "updatedAt", user.getUpdatedAt())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated successfully");
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user", e);
                    callback.onError("Lỗi khi cập nhật thông tin người dùng: " + e.getMessage());
                });
    }

    @Override
    public void getUserById(String userId, UserCallback callback) {
        if (userId == null) {
            callback.onError("User ID is null");
            return;
        }

        db.collection(COLLECTION_USERS).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Không thể chuyển đổi dữ liệu người dùng");
                        }
                    } else {
                        callback.onError("Không tìm thấy người dùng");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user", e);
                    callback.onError("Lỗi khi lấy thông tin người dùng: " + e.getMessage());
                });
    }

    @Override
    public void updateUserStats(String userId, User.UserStats stats, UserCallback callback) {
        if (userId == null || stats == null) {
            callback.onError("User ID or Stats is null");
            return;
        }

        db.collection(COLLECTION_USERS).document(userId)
                .update(
                        "stats", stats,
                        "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User stats updated successfully");
                    getUserById(userId, callback); // Lấy lại thông tin user sau khi cập nhật
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user stats", e);
                    callback.onError("Lỗi khi cập nhật thống kê người dùng: " + e.getMessage());
                });
    }

    @Override
    public void addHeritageToUser(String userId, String heritageId, UserCallback callback) {
        if (userId == null || heritageId == null) {
            callback.onError("User ID or Heritage ID is null");
            return;
        }

        db.collection(COLLECTION_USERS).document(userId)
                .update(
                        "heritageIds", com.google.firebase.firestore.FieldValue.arrayUnion(heritageId),
                        "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Heritage added to user successfully");
                    getUserById(userId, callback); // Lấy lại thông tin user sau khi cập nhật
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding heritage to user", e);
                    callback.onError("Lỗi khi thêm di tích vào danh sách: " + e.getMessage());
                });
    }
}