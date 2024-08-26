package com.example.green_action;

public class Post {
    public String postId;
    public String title;
    public String content;
    public String authorId;
    public long timestamp;
    public String imageUrl;
    public int likes;
    public String category;

    public Post() {}

    public Post(String postId, String title, String content, String authorId, long timestamp, String imageUrl, int likes, String category) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.category = category;
    }
}