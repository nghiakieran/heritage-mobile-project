package hcmute.edu.vn.heritageproject.models;

public class HeroSlide {
    private int id;
    private String image;
    private String title;
    private String subTitle;

    public HeroSlide(int id, String image, String title, String subTitle) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.subTitle = subTitle;
    }

    public int getId() { return id; }
    public String getImage() { return image; }
    public String getTitle() { return title; }
    public String getSubTitle() { return subTitle; }
}
