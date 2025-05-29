package hcmute.edu.vn.heritageproject.api.models;

import java.util.List;

public class AppCache {
    private static List<Heritage> heritageList = null;

    public static List<Heritage> getHeritageList() {
        return heritageList;
    }

    public static void setHeritageList(List<Heritage> list) {
        heritageList = list;
    }

    public static void clearHeritageList() {
        heritageList = null;
    }
} 