package com.example.green_action.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.green_action.User;
import com.example.green_action.Ranking;
import com.example.green_action.DailyQuiz;
import com.example.green_action.Post;
import com.example.green_action.Comment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseClient {

    private final FirebaseDatabase database;
    private final DatabaseReference dbRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference dailyQuizRef;

    private static final String TAG = "FirebaseClient";

    public FirebaseClient() {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        usersRef = dbRef.child("users");
        dailyQuizRef = dbRef.child("daily_quiz");
    }

    // boardType에 따른 게시글 참조 가져오기
    public DatabaseReference getPostsRef(String boardType) {
        switch (boardType) {
            case "issue":
                return dbRef.child("issue_posts");
            case "free":
                return dbRef.child("free_posts");
            case "notice":
                return dbRef.child("notice_posts");
            case "qna":
                return dbRef.child("qna_posts");
            default:
                throw new IllegalArgumentException("Invalid board type: " + boardType);
        }
    }

    // 사용자 데이터를 Firebase에 저장하는 메서드
    public void saveUserData(String userId, User user) {
        if (userId != null && user != null) {
            usersRef.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User data saved successfully.");
                } else {
                    Log.e(TAG, "Failed to save user data", task.getException());
                }
            });
        }
    }

    // 사용자 데이터를 불러오는 메서드
    public void loadUserData(String userId, ValueEventListener listener) {
        if (userId != null && !userId.isEmpty()) {
            Log.d(TAG, "Loading user data for userId: " + userId);
            DatabaseReference userRef = usersRef.child(userId);
            userRef.addListenerForSingleValueEvent(listener);
        } else {
            Log.e(TAG, "User ID is null or empty");
            listener.onDataChange(null);
        }
    }

    // 아이디 중복 확인 함수 (Firebase에서 체크)
    public void isIDExists(String id, final OnCheckUserExistsListener listener) {
        usersRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onCheck(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onCheck(false);
            }
        });
    }

    // 퀴즈 진행 상태 전체를 Firebase에서 불러오는 메서드
    public void loadAllQuizProgress(String userId, ValueEventListener listener) {
        DatabaseReference quizProgressRef = dbRef.child("users").child(userId).child("quiz_progress");
        quizProgressRef.addListenerForSingleValueEvent(listener);
    }

    // 게시글을 저장하는 메서드
    public void savePostData(String boardType, String postId, Post post) {
        getPostsRef(boardType).child(postId).setValue(post).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Post data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save post data", task.getException());
            }
        });
    }

    // 사용자 데이터 참조 반환 메서드
    public DatabaseReference getUsersRef() {
        return usersRef;
    }

    // 게시글 데이터를 Firebase에서 불러오는 메서드
    public void loadPostData(String boardType, String postId, ValueEventListener listener) {
        getPostsRef(boardType).child(postId).addListenerForSingleValueEvent(listener);
    }

    // 댓글 참조 가져오기
    public DatabaseReference getCommentsRef(String boardType, String postId) {
        return getPostsRef(boardType).child(postId).child("comments");
    }

    // 댓글을 저장하는 메서드
    public void saveCommentData(String boardType, String postId, String commentId, Comment comment) {
        getCommentsRef(boardType, postId).child(commentId).setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Comment data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save comment data", task.getException());
            }
        });
    }

    // 댓글 데이터를 Firebase에서 불러오는 메서드
    public void loadCommentData(String boardType, String postId, String commentId, ValueEventListener listener) {
        getCommentsRef(boardType, postId).child(commentId).addListenerForSingleValueEvent(listener);
    }

    // 랭킹 데이터를 저장하는 메서드
    public void saveRankingData(String userId, Ranking rank) {
        dbRef.child("ranking").child(userId).setValue(rank).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Ranking data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save ranking data", task.getException());
            }
        });
    }

    // 랭킹 데이터를 Firebase에서 불러오는 메서드
    public void loadRankingData(String userId, ValueEventListener listener) {
        dbRef.child("ranking").child(userId).addListenerForSingleValueEvent(listener);
    }

    // 일일 퀴즈를 저장하는 메서드
    public void saveDailyQuizData(String quizId, DailyQuiz dailyQuiz) {
        dailyQuizRef.child(quizId).setValue(dailyQuiz).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Daily quiz data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save daily quiz data", task.getException());
            }
        });
    }

    // 일일 퀴즈 데이터를 Firebase에서 불러오는 메서드
    public void loadDailyQuizData(String quizId, ValueEventListener listener) {
        dailyQuizRef.child(quizId).addListenerForSingleValueEvent(listener);
    }

    // 퀴즈 진행 상태 저장 메서드
    public void saveQuizProgress(String userId, int quizId, boolean isSolved) {
        dbRef.child("users").child(userId).child("quiz_progress").child(String.valueOf(quizId))
                .setValue(isSolved ? 1 : 0).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Quiz progress saved successfully.");
                    } else {
                        Log.e(TAG, "Failed to save quiz progress", task.getException());
                    }
                });
    }

    // 인터페이스: 아이디 중복 확인 결과를 위한 콜백
    public interface OnCheckUserExistsListener {
        void onCheck(boolean exists);
    }
}
