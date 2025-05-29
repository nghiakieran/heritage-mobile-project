package hcmute.edu.vn.heritageproject.models;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class User {
    private String id;
    private String displayName;
    private String email;
    private String photoUrl;
    private List<String> heritageIds; // Danh sách ID các di tích đã thăm
    private UserStats stats;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructor mặc định cần thiết cho Firestore
    public User() {
    }

    public User(String id, String displayName, String email, String photoUrl) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.stats = new UserStats();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<String> getHeritageIds() {
        return heritageIds;
    }

    public void setHeritageIds(List<String> heritageIds) {
        this.heritageIds = heritageIds;
    }

    public UserStats getStats() {
        return stats;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Inner class cho thống kê người dùng
    public static class UserStats {
        private int totalVisitedHeritages;
        private int totalCompletedTests;
        private double averageScore;
        private int totalReviews;

        public UserStats() {
            this.totalVisitedHeritages = 0;
            this.totalCompletedTests = 0;
            this.averageScore = 0.0;
            this.totalReviews = 0;
        }

        // Getters và Setters
        public int getTotalVisitedHeritages() {
            return totalVisitedHeritages;
        }

        public void setTotalVisitedHeritages(int totalVisitedHeritages) {
            this.totalVisitedHeritages = totalVisitedHeritages;
        }

        public int getTotalCompletedTests() {
            return totalCompletedTests;
        }

        public void setTotalCompletedTests(int totalCompletedTests) {
            this.totalCompletedTests = totalCompletedTests;
        }

        public double getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(double averageScore) {
            this.averageScore = averageScore;
        }

        public int getTotalReviews() {
            return totalReviews;
        }

        public void setTotalReviews(int totalReviews) {
            this.totalReviews = totalReviews;
        }
    }
}