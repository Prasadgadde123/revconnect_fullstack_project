package com.revconnect.dto.request;

public class UserSearchRequest {
    private String query = "";
    private String userType; // PERSONAL, CREATOR, BUSINESS
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "username";
    private String sortDirection = "asc";

    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}