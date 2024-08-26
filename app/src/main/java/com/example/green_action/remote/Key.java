package com.example.green_action.remote;

import com.example.green_action.Comment;
import com.example.green_action.DailyQuiz;
import com.example.green_action.Post;
import com.example.green_action.QuizDetail;
import com.example.green_action.Ranking;
import com.example.green_action.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Key {
    private final DatabaseReference databaseReference;

    public Key() {
        // Firebase Realtime Database의 기본 경로 설정
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
    }

    // 사용자 데이터 쓰기
    public void writeUserData(String userId, User user) {
        databaseReference.child("users").child(userId).setValue(user);
    }

    // 특정 사용자 참조 가져오기
    public DatabaseReference getUserReference(String userId) {
        return databaseReference.child("users").child(userId);
    }

    // 게시글 데이터 쓰기
    public void writePostData(String postId, Post post) {
        databaseReference.child("posts").child(postId).setValue(post);
    }

    // 특정 게시글 참조 가져오기
    public DatabaseReference getPostReference(String postId) {
        return databaseReference.child("posts").child(postId);
    }

    // 댓글 데이터 쓰기
    public void writeCommentData(String commentId, Comment comment) {
        databaseReference.child("comments").child(commentId).setValue(comment);
    }

    // 특정 댓글 참조 가져오기
    public DatabaseReference getCommentReference(String commentId) {
        return databaseReference.child("comments").child(commentId);
    }

    // 랭킹 데이터 쓰기
    public void writeRankingData(String userId, Ranking ranking) {
        databaseReference.child("ranking").child(userId).setValue(ranking);
    }

    // 특정 사용자의 랭킹 참조 가져오기
    public DatabaseReference getRankingReference(String userId) {
        return databaseReference.child("ranking").child(userId);
    }

    // 퀴즈 데이터 쓰기 (퀴즈 문제, 정답, 해설 포함)
    public void writeQuizDetail(String quizId, QuizDetail quizDetail) {
        databaseReference.child("quizzes").child(quizId).setValue(quizDetail);
    }

    // 퀴즈 데이터 읽기
    public DatabaseReference getQuizDetailReference(String quizId) {
        return databaseReference.child("quizzes").child(quizId);
    }

    // 퀴즈 목록 참조 가져오기
    public DatabaseReference getAllQuizDetailsReference() {
        return databaseReference.child("quizzes");
    }

    // 데이터 삭제 메서드
    public void deleteData(String path) {
        databaseReference.child(path).removeValue();
    }

    // 실시간 데이터 변경 감지 메서드
    public void addValueEventListenerToUser(String userId, ValueEventListener listener) {
        databaseReference.child("users").child(userId).addValueEventListener(listener);
    }

    public void addChildEventListenerToPosts(ChildEventListener listener) {
        databaseReference.child("posts").addChildEventListener(listener);
    }

    // 특정 필드에 대한 쿼리 예시 (예: 특정 점수 이상인 사용자 가져오기)
    public Query getUsersWithScoreAbove(int score) {
        return databaseReference.child("users").orderByChild("score").startAt(score);
    }
}