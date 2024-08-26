package com.example.green_action.Community;

public class CommunityPostItem {
    private String postId;
    private String userId;
    private String title;
    private String content;
    private String timestamp;
    private String username;  // 새로운 필드 추가
    private int likes;  // likes 필드 추가
    private String boardType;  // 게시판 유형을 나타내는 필드 추가

    public CommunityPostItem() {
        // 기본 생성자
    }

    public CommunityPostItem(String postId, String userId, String title, String content, String timestamp, String username) {
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.username = username;
    }

    // Getter and Setter methods
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }
}
