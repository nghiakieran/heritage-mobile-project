package hcmute.edu.vn.heritageproject.api.models;

import java.util.ArrayList;
import java.util.List;

public class Heritage {
    private String id;
    private String name;
    private String nameSlug;
    private String description;
    private List<String> images;
    private String location;
    private String locationSlug;
    private String locationNormalized;
    private Coordinates coordinates;
    private Stats stats;
    private String knowledgeTestId;
    private String leaderboardId;
    private LeaderboardSummary leaderboardSummary;
    private KnowledgeTestSummary knowledgeTestSummary;
    private List<String> rolePlayIds;
    private AdditionalInfo additionalInfo;
    private String status;
    private List<String> popularTags;
    private List<String> tagsSlug;
    private long createdAt;
    private long updatedAt;
    private double distance; // Sử dụng cho tính năng Explore/gần nhất
    
    public Heritage() {
        this.images = new ArrayList<>();
        this.popularTags = new ArrayList<>();
        this.tagsSlug = new ArrayList<>();
        this.rolePlayIds = new ArrayList<>();
    }
    
    // Inner classes để phù hợp với JSON schema từ backend
    public static class Coordinates {
        private String latitude;
        private String longitude;
        
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
    }
    
    public static class Stats {
        private String averageRating;
        private String totalReviews;
        private String totalVisits;
        private String totalFavorites;
        
        public String getAverageRating() { return averageRating; }
        public void setAverageRating(String averageRating) { this.averageRating = averageRating; }
        
        public String getTotalReviews() { return totalReviews; }
        public void setTotalReviews(String totalReviews) { this.totalReviews = totalReviews; }
        
        public String getTotalVisits() { return totalVisits; }
        public void setTotalVisits(String totalVisits) { this.totalVisits = totalVisits; }
        
        public String getTotalFavorites() { return totalFavorites; }
        public void setTotalFavorites(String totalFavorites) { this.totalFavorites = totalFavorites; }
    }
    
    public static class TopUser {
        private String userId;
        private String userName;
        private int score;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
    }
    
    public static class LeaderboardSummary {
        private String topScore;
        private List<TopUser> topUsers;
        private String totalParticipants;
        
        public String getTopScore() { return topScore; }
        public void setTopScore(String topScore) { this.topScore = topScore; }
        
        public List<TopUser> getTopUsers() { return topUsers; }
        public void setTopUsers(List<TopUser> topUsers) { this.topUsers = topUsers; }
        
        public String getTotalParticipants() { return totalParticipants; }
        public void setTotalParticipants(String totalParticipants) { this.totalParticipants = totalParticipants; }
    }
    
    public static class KnowledgeTestSummary {
        private String title;
        private String questionCount;
        private String difficulty;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getQuestionCount() { return questionCount; }
        public void setQuestionCount(String questionCount) { this.questionCount = questionCount; }
        
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    }
    
    public static class HistoricalEvent {
        private String title;
        private String description;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    public static class AdditionalInfo {
        private String architectural;
        private String culturalFestival;
        private List<HistoricalEvent> historicalEvents;
        
        public String getArchitectural() { return architectural; }
        public void setArchitectural(String architectural) { this.architectural = architectural; }
        
        public String getCulturalFestival() { return culturalFestival; }
        public void setCulturalFestival(String culturalFestival) { this.culturalFestival = culturalFestival; }
        
        public List<HistoricalEvent> getHistoricalEvents() { return historicalEvents; }
        public void setHistoricalEvents(List<HistoricalEvent> historicalEvents) { this.historicalEvents = historicalEvents; }
    }
    
    // Basic getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getNameSlug() { return nameSlug; }
    public void setNameSlug(String nameSlug) { this.nameSlug = nameSlug; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getLocationSlug() { return locationSlug; }
    public void setLocationSlug(String locationSlug) { this.locationSlug = locationSlug; }
    
    public String getLocationNormalized() { return locationNormalized; }
    public void setLocationNormalized(String locationNormalized) { this.locationNormalized = locationNormalized; }
    
    public Coordinates getCoordinates() { return coordinates; }
    public void setCoordinates(Coordinates coordinates) { this.coordinates = coordinates; }
    
    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }
    
    public String getKnowledgeTestId() { return knowledgeTestId; }
    public void setKnowledgeTestId(String knowledgeTestId) { this.knowledgeTestId = knowledgeTestId; }
    
    public String getLeaderboardId() { return leaderboardId; }
    public void setLeaderboardId(String leaderboardId) { this.leaderboardId = leaderboardId; }
    
    public LeaderboardSummary getLeaderboardSummary() { return leaderboardSummary; }
    public void setLeaderboardSummary(LeaderboardSummary leaderboardSummary) { this.leaderboardSummary = leaderboardSummary; }
    
    public KnowledgeTestSummary getKnowledgeTestSummary() { return knowledgeTestSummary; }
    public void setKnowledgeTestSummary(KnowledgeTestSummary knowledgeTestSummary) { this.knowledgeTestSummary = knowledgeTestSummary; }
    
    public List<String> getRolePlayIds() { return rolePlayIds; }
    public void setRolePlayIds(List<String> rolePlayIds) { this.rolePlayIds = rolePlayIds; }
    
    public AdditionalInfo getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(AdditionalInfo additionalInfo) { this.additionalInfo = additionalInfo; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<String> getPopularTags() { return popularTags; }
    public void setPopularTags(List<String> popularTags) { this.popularTags = popularTags; }
    
    public List<String> getTagsSlug() { return tagsSlug; }
    public void setTagsSlug(List<String> tagsSlug) { this.tagsSlug = tagsSlug; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
}
