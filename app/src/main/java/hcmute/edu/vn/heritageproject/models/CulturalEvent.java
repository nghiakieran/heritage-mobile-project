package hcmute.edu.vn.heritageproject.models;

public class CulturalEvent {
    private String name;
    private String date;
    private String location;
    private String description;
    private int imageResId;

    public CulturalEvent(String name, String date, String location, String description, int imageResId) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }
}
