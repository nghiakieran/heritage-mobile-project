package hcmute.edu.vn.heritageproject.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HeritageResponse {
    private boolean success;
    private String message;
    private List<Heritage> heritages;
    private Pagination pagination;
    private int totalItems;
    private List<HeritageNameResponse> heritageNames;

    // Trường bổ sung để parse response trực tiếp từ /heritages/id/:id
    @SerializedName("_id")
    private String id;
    private String name;
    private String description;
    private List<String> images;
    private String location;
    private Heritage.Coordinates coordinates;  // Sử dụng inner class từ Heritage
    private Heritage.Stats stats;              // Sử dụng inner class từ Heritage
    @SerializedName("knowledgeTestId")
    private String knowledgeTestId;
    @SerializedName("leaderboardId")
    private String leaderboardId;
    @SerializedName("leaderboardSummary")
    private Heritage.LeaderboardSummary leaderboardSummary;  // Sử dụng inner class từ Heritage
    @SerializedName("knowledgeTestSummary")
    private Heritage.KnowledgeTestSummary knowledgeTestSummary;  // Sử dụng inner class từ Heritage
    @SerializedName("rolePlayIds")
    private List<String> rolePlayIds;
    @SerializedName("additionalInfo")
    private Heritage.AdditionalInfo additionalInfo;  // Sử dụng inner class từ Heritage
    private String status;
    @SerializedName("popularTags")
    private List<String> popularTags;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("updatedAt")
    private String updatedAt;
    @SerializedName("locationSlug")
    private String locationSlug;
    @SerializedName("nameSlug")
    private String nameSlug;
    @SerializedName("tagsSlug")
    private List<String> tagsSlug;

    public static class Pagination {
        private int totalItems;
        private int currentPage;
        private int totalPages;
        private int itemsPerPage;

        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getItemsPerPage() { return itemsPerPage; }
        public void setItemsPerPage(int itemsPerPage) { this.itemsPerPage = itemsPerPage; }
    }

    public HeritageResponse() {}

    // Basic getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<Heritage> getHeritages() { return heritages; }
    public void setHeritages(List<Heritage> heritages) { this.heritages = heritages; }
    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    public List<HeritageNameResponse> getHeritageNames() { return heritageNames; }
    public void setHeritageNames(List<HeritageNameResponse> heritageNames) { this.heritageNames = heritageNames; }

    // Direct response fields getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Heritage.Coordinates getCoordinates() { return coordinates; }
    public void setCoordinates(Heritage.Coordinates coordinates) { this.coordinates = coordinates; }
    public Heritage.Stats getStats() { return stats; }
    public void setStats(Heritage.Stats stats) { this.stats = stats; }
    public String getKnowledgeTestId() { return knowledgeTestId; }
    public void setKnowledgeTestId(String knowledgeTestId) { this.knowledgeTestId = knowledgeTestId; }
    public String getLeaderboardId() { return leaderboardId; }
    public void setLeaderboardId(String leaderboardId) { this.leaderboardId = leaderboardId; }
    public Heritage.LeaderboardSummary getLeaderboardSummary() { return leaderboardSummary; }
    public void setLeaderboardSummary(Heritage.LeaderboardSummary leaderboardSummary) { this.leaderboardSummary = leaderboardSummary; }
    public Heritage.KnowledgeTestSummary getKnowledgeTestSummary() { return knowledgeTestSummary; }
    public void setKnowledgeTestSummary(Heritage.KnowledgeTestSummary knowledgeTestSummary) { this.knowledgeTestSummary = knowledgeTestSummary; }
    public List<String> getRolePlayIds() { return rolePlayIds; }
    public void setRolePlayIds(List<String> rolePlayIds) { this.rolePlayIds = rolePlayIds; }
    public Heritage.AdditionalInfo getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(Heritage.AdditionalInfo additionalInfo) { this.additionalInfo = additionalInfo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getPopularTags() { return popularTags; }
    public void setPopularTags(List<String> popularTags) { this.popularTags = popularTags; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getLocationSlug() { return locationSlug; }
    public void setLocationSlug(String locationSlug) { this.locationSlug = locationSlug; }
    public String getNameSlug() { return nameSlug; }
    public void setNameSlug(String nameSlug) { this.nameSlug = nameSlug; }
    public List<String> getTagsSlug() { return tagsSlug; }
    public void setTagsSlug(List<String> tagsSlug) { this.tagsSlug = tagsSlug; }
}