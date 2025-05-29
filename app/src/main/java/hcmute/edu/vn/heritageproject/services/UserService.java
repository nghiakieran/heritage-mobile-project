package hcmute.edu.vn.heritageproject.services;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import hcmute.edu.vn.heritageproject.models.User;
import hcmute.edu.vn.heritageproject.repository.FirebaseUserRepository;
import hcmute.edu.vn.heritageproject.repository.UserRepository;

public class UserService {
    private static final String TAG = "UserService";
    private static UserService instance;
    private final FirebaseAuth auth;
    private final UserRepository userRepository;

    private UserService() {
        auth = FirebaseAuth.getInstance();
        userRepository = FirebaseUserRepository.getInstance();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public interface UserServiceCallback {
        void onSuccess(User user);

        void onError(String error);
    }

    public void handleUserLogin(FirebaseUser firebaseUser, UserServiceCallback callback) {
        if (firebaseUser == null) {
            callback.onError("FirebaseUser is null");
            return;
        }

        String userId = firebaseUser.getUid();
        userRepository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // User đã tồn tại, cập nhật thông tin cơ bản
                user.setDisplayName(firebaseUser.getDisplayName());
                user.setEmail(firebaseUser.getEmail());
                user.setPhotoUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);

                userRepository.updateUser(user, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User updatedUser) {
                        callback.onSuccess(updatedUser);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                // User chưa tồn tại, tạo mới
                userRepository.createUser(firebaseUser, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User newUser) {
                        callback.onSuccess(newUser);
                    }

                    @Override
                    public void onError(String createError) {
                        callback.onError(createError);
                    }
                });
            }
        });
    }

    public void getCurrentUser(UserServiceCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        userRepository.getUserById(firebaseUser.getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void updateUserStats(User.UserStats stats, UserServiceCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        userRepository.updateUserStats(firebaseUser.getUid(), stats, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void addHeritageToUser(String heritageId, UserServiceCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        userRepository.addHeritageToUser(firebaseUser.getUid(), heritageId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void updateUser(User user, UserServiceCallback callback) {
        if (user == null || user.getId() == null) {
            callback.onError("Không thể cập nhật thông tin người dùng");
            return;
        }

        userRepository.updateUser(user, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User updatedUser) {
                callback.onSuccess(updatedUser);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}