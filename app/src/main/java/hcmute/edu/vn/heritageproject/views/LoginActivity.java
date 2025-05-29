package hcmute.edu.vn.heritageproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.databinding.ActivityLoginBinding;
import hcmute.edu.vn.heritageproject.models.User;
import hcmute.edu.vn.heritageproject.services.UserService;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth và UserService
        mAuth = FirebaseAuth.getInstance();
        userService = UserService.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configure Google Sign In Launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "Google sign in success");
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.e(TAG, "Google sign in failed", e);
                            String errorMessage = "Đăng nhập Google thất bại: ";
                            switch (e.getStatusCode()) {
                                case 10:
                                    errorMessage += "Vui lòng kiểm tra lại cấu hình SHA-1 trong Firebase Console";
                                    break;
                                case 7:
                                    errorMessage += "Không có kết nối mạng";
                                    break;
                                case 12500:
                                    errorMessage += "Vui lòng kiểm tra lại cấu hình OAuth 2.0";
                                    break;
                                default:
                                    errorMessage += e.getMessage();
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(TAG, "Google sign in cancelled");
                        Toast.makeText(LoginActivity.this, "Đăng nhập Google bị hủy", Toast.LENGTH_SHORT).show();
                    }
                });

        setupClickListeners();

        // Kiểm tra nếu activity được khởi chạy từ màn hình chính
        if (getIntent().getBooleanExtra("fromMainActivity", false)) {
            // Không tự động chuyển đến MainActivity nếu được mở từ MainActivity
            // để người dùng có thể đăng nhập lại
        } else {
            // Kiểm tra người dùng đã đăng nhập chưa
            checkCurrentUser();
        }
    }

    private void checkCurrentUser() {
        // Kiểm tra nếu người dùng đã đăng nhập
        if (mAuth.getCurrentUser() != null) {
            startMainActivity();
        }
    }

    private void setupClickListeners() {
        binding.googleSignInCard.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        // Đăng xuất khỏi Google trước để hiển thị dialog chọn tài khoản
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            // Sau khi đăng xuất, hiển thị dialog chọn tài khoản
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase Google auth success");
                        FirebaseUser firebaseUser = task.getResult().getUser();

                        // Kiểm tra xem user đã tồn tại trong Firestore chưa
                        userService.getCurrentUser(new UserService.UserServiceCallback() {
                            @Override
                            public void onSuccess(User existingUser) {
                                // Nếu user đã tồn tại, sử dụng thông tin từ Firestore
                                Log.d(TAG, "User exists in Firestore, using existing data");
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                startMainActivity();
                            }

                            @Override
                            public void onError(String error) {
                                // Nếu user chưa tồn tại, tạo mới user
                                Log.d(TAG, "User does not exist in Firestore, creating new user");
                                userService.handleUserLogin(firebaseUser, new UserService.UserServiceCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        Log.d(TAG, "New user created successfully");
                                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT)
                                                .show();
                                        startMainActivity();
                                    }

                                    @Override
                                    public void onError(String createError) {
                                        Log.e(TAG, "Error creating new user: " + createError);
                                        Toast.makeText(LoginActivity.this, "Lỗi: " + createError, Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                            }
                        });
                    } else {
                        Log.e(TAG, "Firebase Google auth failed", task.getException());
                        String errorMessage = "Đăng nhập thất bại: ";
                        if (task.getException() != null) {
                            errorMessage += task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa activity stack
        startActivity(intent);
        finish();
    }
}