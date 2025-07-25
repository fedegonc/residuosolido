package com.residuosolido.app.model;

public class Post {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private String sourceUrl;
    private String sourceName;
    
    // Constructores
    public Post() {}
    
    public Post(Long id, String title, String content, String imageUrl, Long categoryId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }
    
    public Post(Long id, String title, String content, String imageUrl, Long categoryId, String sourceUrl, String sourceName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.sourceUrl = sourceUrl;
        this.sourceName = sourceName;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
}
