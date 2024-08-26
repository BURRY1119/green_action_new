package com.example.green_action;

public class Comment {
    public String commentId;
    public String postId;
    public String authorId;
    public String content;
    public long timestamp;
    public String parentCommentId; // 상위 댓글 ID (대댓글인 경우 참조, 일반 댓글인 경우 null)

    public Comment() {}

    public Comment(String commentId, String postId, String authorId, String content, long timestamp, String parentCommentId) {
        this.commentId = commentId;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.timestamp = timestamp;
        this.parentCommentId = parentCommentId;
    }
}