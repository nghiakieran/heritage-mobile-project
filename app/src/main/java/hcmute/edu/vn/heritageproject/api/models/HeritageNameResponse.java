package hcmute.edu.vn.heritageproject.api.models;

/**
 * Model class for heritage name response from API
 */
public class HeritageNameResponse {
    private String _id;
    private String name;
    
    public HeritageNameResponse() {
    }
    
    public String getId() {
        return _id;
    }
    
    public void setId(String id) {
        this._id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
