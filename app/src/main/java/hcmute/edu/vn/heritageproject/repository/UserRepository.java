package hcmute.edu.vn.heritageproject.repository;

import com.google.firebase.auth.FirebaseUser;
import hcmute.edu.vn.heritageproject.models.User;

public interface UserRepository {
    interface UserCallback {
        void onSuccess(User user);

        void onError(String error);
    }

    void createUser(FirebaseUser firebaseUser, UserCallback callback);

    void updateUser(User user, UserCallback callback);

    void getUserById(String userId, UserCallback callback);

    void updateUserStats(String userId, User.UserStats stats, UserCallback callback);

    void addHeritageToUser(String userId, String heritageId, UserCallback callback);
}