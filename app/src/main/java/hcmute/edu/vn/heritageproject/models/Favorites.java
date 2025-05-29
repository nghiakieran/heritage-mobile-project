package hcmute.edu.vn.heritageproject.models;

import com.google.firebase.Timestamp;
import java.util.List;

public class Favorites {
    private String id;
    private String userId;
    private List<FavoriteItem> items;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Favorites() {
    }

    public Favorites(String id, String userId, List<FavoriteItem> items) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<FavoriteItem> getItems() {
        return items;
    }

    public void setItems(List<FavoriteItem> items) {
        this.items = items;
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

    public static class FavoriteItem {
        private String heritageId;
        private Timestamp addedAt;

        public FavoriteItem() {
        }

        public FavoriteItem(String heritageId) {
            this.heritageId = heritageId;
            this.addedAt = Timestamp.now();
        }

        public String getHeritageId() {
            return heritageId;
        }

        public void setHeritageId(String heritageId) {
            this.heritageId = heritageId;
        }

        public Timestamp getAddedAt() {
            return addedAt;
        }

        public void setAddedAt(Timestamp addedAt) {
            this.addedAt = addedAt;
        }
    }
}