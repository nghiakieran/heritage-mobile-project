package hcmute.edu.vn.heritageproject.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.models.User;
import hcmute.edu.vn.heritageproject.services.UserService;

public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText displayNameEditText;
    private TextInputEditText emailEditText;
    private Button saveButton;
    private UserService userService;
    private User currentUser;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize services
        userService = UserService.getInstance();

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Find views
        displayNameEditText = findViewById(R.id.displayNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveButton = findViewById(R.id.saveButton);

        // Set click listeners
        saveButton.setOnClickListener(v -> saveUserData());

        // Load current user data
        loadUserData();
    }

    private void loadUserData() {
        userService.getCurrentUser(new UserService.UserServiceCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                displayUserData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayUserData() {
        if (currentUser != null) {
            displayNameEditText.setText(currentUser.getDisplayName());
            emailEditText.setText(currentUser.getEmail());
        }
    }

    private void saveUserData() {
        if (currentUser == null) {
            Toast.makeText(this, "Không thể cập nhật thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate data
        String displayName = displayNameEditText.getText().toString().trim();
        if (displayName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên hiển thị", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        saveButton.setEnabled(false);
        saveButton.setText("Đang lưu...");

        // Update user data
        currentUser.setDisplayName(displayName);

        // Save to database
        userService.updateUser(currentUser, new UserService.UserServiceCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
                saveButton.setText("Lưu thay đổi");
            }
        });
    }
}