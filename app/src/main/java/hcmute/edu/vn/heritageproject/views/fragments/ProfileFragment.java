package hcmute.edu.vn.heritageproject.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hcmute.edu.vn.heritageproject.R;
import hcmute.edu.vn.heritageproject.databinding.FragmentProfileBinding;
import hcmute.edu.vn.heritageproject.models.User;
import hcmute.edu.vn.heritageproject.services.UserService;
import hcmute.edu.vn.heritageproject.views.EditProfileActivity;
import hcmute.edu.vn.heritageproject.views.LoginActivity;
import hcmute.edu.vn.heritageproject.views.FavoritesActivity;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private UserService userService;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        userService = UserService.getInstance();

        // Ẩn tất cả các view ban đầu
        binding.profileContent.setVisibility(View.GONE);
        binding.loginPrompt.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Kiểm tra trạng thái đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Người dùng đã đăng nhập, lấy thông tin từ Firestore
            userService.getCurrentUser(new UserService.UserServiceCallback() {
                @Override
                public void onSuccess(User user) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.profileContent.setVisibility(View.VISIBLE);
                    updateUI(user);
                }

                @Override
                public void onError(String error) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.loginPrompt.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error getting user data: " + error);
                }
            });
        } else {
            // Người dùng chưa đăng nhập
            binding.progressBar.setVisibility(View.GONE);
            binding.loginPrompt.setVisibility(View.VISIBLE);
        }

        setupClickListeners();
        return binding.getRoot();
    }

    private void updateUI(User user) {
        if (user != null) {
            // Hiển thị thông tin người dùng
            binding.userNameTextView.setText(user.getDisplayName());
            binding.userEmailTextView.setText(user.getEmail());

            // Hiển thị ảnh đại diện nếu có
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .into(binding.profileImageView);
            } else {
                // Sử dụng ảnh mặc định nếu không có ảnh
                binding.profileImageView.setImageResource(R.drawable.default_avatar);
            }

            // Hiển thị thống kê
            User.UserStats stats = user.getStats();
            if (stats != null) {
                binding.totalVisitedTextView.setText(String.valueOf(stats.getTotalVisitedHeritages()));
                binding.totalTestsTextView.setText(String.valueOf(stats.getTotalCompletedTests()));
                binding.averageScoreTextView.setText(String.format("%.1f", stats.getAverageScore()));
                binding.totalReviewsTextView.setText(String.valueOf(stats.getTotalReviews()));
            }
        }
    }

    private void setupClickListeners() {
        binding.loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.putExtra("fromMainActivity", true);
            startActivity(intent);
        });

        binding.editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        binding.signOutButton.setOnClickListener(v -> signOut());

        // Thêm click listener cho card danh sách yêu thích
        MaterialCardView favoritesCard = binding.getRoot().findViewById(R.id.favoritesCard);
        favoritesCard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FavoritesActivity.class);
            startActivity(intent);
        });
    }

    private void signOut() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    binding.profileContent.setVisibility(View.GONE);
                    binding.loginPrompt.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
