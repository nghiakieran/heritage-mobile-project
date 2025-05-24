package hcmute.edu.vn.heritageproject.api.models;

import java.util.List;

public class HeritageResponse {
    private boolean success;
    private String message;
    private List<Heritage> heritages;
    private Pagination pagination;
    private int totalItems;  // Trường bổ sung cho API explore
    private List<HeritageNameResponse> heritageNames;
    
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
    
    public HeritageResponse() {
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<Heritage> getHeritages() {
        return heritages;
    }
    
    public void setHeritages(List<Heritage> heritages) {
        this.heritages = heritages;
    }
    
    public Pagination getPagination() {
        return pagination;
    }
    
    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    
    public List<HeritageNameResponse> getHeritageNames() {
        return heritageNames;
    }
    
    public void setHeritageNames(List<HeritageNameResponse> heritageNames) {
        this.heritageNames = heritageNames;
    }
}
