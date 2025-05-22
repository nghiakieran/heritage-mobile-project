package hcmute.edu.vn.heritageproject.models;

public class Banner {
    private String title;
    private String description;
    private int imageResId;

    public Banner(String title, String description, int imageResId) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}
