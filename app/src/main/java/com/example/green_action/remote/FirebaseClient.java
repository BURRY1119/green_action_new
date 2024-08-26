package com.example.green_action.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.green_action.DataBaseHandler;
import com.example.green_action.User;
import com.example.green_action.Ranking;
import com.example.green_action.DailyQuiz;
import com.example.green_action.QuizDetail;
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
    private final DatabaseReference postsRef;
    private final DatabaseReference dailyQuizRef;

    private static final String TAG = "FirebaseClient";

    public FirebaseClient() {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        usersRef = dbRef.child("users");
        postsRef = dbRef.child("posts");
        dailyQuizRef = dbRef.child("daily_quiz");
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
    public void isIDExists(String id, final DataBaseHandler.OnCheckUserExistsListener listener) {
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

    // 게시글 참조 가져오기
    public DatabaseReference getPostsRef() {
        return postsRef;
    }

    // 퀴즈 진행 상태 전체를 Firebase에서 불러오는 메서드
    public void loadAllQuizProgress(String userId, ValueEventListener listener) {
        DatabaseReference quizProgressRef = dbRef.child("users").child(userId).child("quiz_progress");
        quizProgressRef.addListenerForSingleValueEvent(listener);
    }

    // 게시글을 저장하는 메서드
    public void savePostData(String postId, Post post) {
        postsRef.child(postId).setValue(post).addOnCompleteListener(task -> {
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
    public void loadPostData(String postId, ValueEventListener listener) {
        postsRef.child(postId).addListenerForSingleValueEvent(listener);
    }

    // 댓글 참조 가져오기
    public DatabaseReference getCommentsRef(String postId) {
        return postsRef.child(postId).child("comments");
    }

    // 댓글을 저장하는 메서드
    public void saveCommentData(String commentId, Comment comment) {
        dbRef.child("comments").child(commentId).setValue(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Comment data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save comment data", task.getException());
            }
        });
    }

    // 댓글 데이터를 Firebase에서 불러오는 메서드
    public void loadCommentData(String commentId, ValueEventListener listener) {
        dbRef.child("comments").child(commentId).addListenerForSingleValueEvent(listener);
    }

    // 랭킹 데이터를 저장하는 메서드
    public void saveRankingData(String rankingId, Ranking ranking) {
        dbRef.child("ranking").child(rankingId).setValue(ranking).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Ranking data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save ranking data", task.getException());
            }
        });
    }

    // 퀴즈 디테일을 Firebase에 저장하는 메서드
    public void saveQuizDetail(String pollutionType, String quizId, QuizDetail quizDetail) {
        DatabaseReference quizDetailRef = dbRef.child("quiz_details").child(pollutionType).child(quizId);
        quizDetailRef.setValue(quizDetail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Quiz detail data saved successfully for " + pollutionType);
            } else {
                Log.e(TAG, "Failed to save quiz detail data for " + pollutionType, task.getException());
            }
        });
    }

    // 퀴즈 디테일을 Firebase에서 불러오는 메서드 (오염 유형별)
    public void loadQuizDetail(String pollutionType, String quizId, ValueEventListener listener) {
        DatabaseReference quizDetailRef = dbRef.child("quiz_details").child(pollutionType).child(quizId);
        quizDetailRef.addListenerForSingleValueEvent(listener);
    }

    // 일일 퀴즈 데이터를 Firebase에 저장하는 메서드
    public void saveDailyQuizData(String dailyQuizId, DailyQuiz dailyQuiz) {
        dailyQuizRef.child(dailyQuizId).setValue(dailyQuiz).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Daily quiz data saved successfully.");
            } else {
                Log.e(TAG, "Failed to save daily quiz data", task.getException());
            }
        });
    }

    // 일일 퀴즈 데이터를 Firebase에서 불러오는 메서드
    public void loadDailyQuizData(String dailyQuizId, ValueEventListener listener) {
        dailyQuizRef.child(dailyQuizId).addListenerForSingleValueEvent(listener);
    }

    // 퀴즈 디테일을 이용한 DailyQuiz 업데이트
    public void getQuizDetailFromDailyQuiz(String pollutionType, DailyQuiz dailyQuiz, ValueEventListener listener) {
        String quizId = String.valueOf(dailyQuiz.getQuizId());
        loadQuizDetail(pollutionType, quizId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                QuizDetail quizDetail = snapshot.getValue(QuizDetail.class);
                if (quizDetail != null) {
                    dailyQuiz.setQuizDetail(quizDetail);
                    listener.onDataChange(snapshot); // or pass quizDetail to your listener
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onCancelled(error); // handle errors
            }
        });
    }

    // 사용자별로 마지막으로 푼 퀴즈 ID를 가져오는 메서드
    public void lastSolvedQuiz(String userId, ValueEventListener listener) {
        if (userId != null && !userId.isEmpty()) {
            DatabaseReference userQuizProgressRef = usersRef.child(userId).child("lastSolvedQuiz");
            userQuizProgressRef.addListenerForSingleValueEvent(listener);
        } else {
            Log.e(TAG, "User ID is null or empty");
            listener.onDataChange(null);
        }
    }

    // 사용자가 특정 퀴즈를 푼 상태를 확인하는 메소드
    public void checkQuizProgress(String userId, int quizNumber, ValueEventListener listener) {
        DatabaseReference quizProgressRef = usersRef.child(userId).child("quiz_progress").child(String.valueOf(quizNumber));
        quizProgressRef.addListenerForSingleValueEvent(listener);
    }

    // 사용자가 푼 퀴즈 진행 상태를 저장하는 메소드
    public void saveQuizProgress(String userId, int quizId, boolean isSolved) {
        if (userId != null && isSolved) { // isSolved가 true인 경우에만 진행 상태를 저장
            DatabaseReference userQuizProgressRef = usersRef.child(userId).child("quiz_progress").child(String.valueOf(quizId));
            userQuizProgressRef.setValue(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Quiz progress saved successfully.");
                } else {
                    Log.e(TAG, "Failed to save quiz progress", task.getException());
                }
            });
        } else if (userId == null) {
            Log.e(TAG, "User ID is null");
        } else {
            Log.d(TAG, "Quiz progress not saved as the answer was incorrect.");
        }
    }

    // 사용자 점수를 업데이트하는 메서드 추가
    public void updateUserScore(String userId, int score) {
        usersRef.child(userId).child("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer currentScore = dataSnapshot.getValue(Integer.class);
                if (currentScore == null) {
                    currentScore = 0;
                }
                usersRef.child(userId).child("score").setValue(currentScore + score);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to update user score", databaseError.toException());
            }
        });
    }

    // 사용자 퀴즈 상태를 Firebase에 저장하는 메서드
    public void saveQuizState(String userId, int quizId, int maxScore, int attemptsLeft) {
        if (userId != null) {
            DatabaseReference userQuizRef = usersRef.child(userId).child("quizzes").child(String.valueOf(quizId));
            userQuizRef.child("maxScore").setValue(maxScore);
            userQuizRef.child("attemptsLeft").setValue(attemptsLeft);
        }
    }

    // 사용자 퀴즈 상태를 Firebase에서 불러오는 메서드
    public void loadQuizState(String userId, int quizId, ValueEventListener listener) {
        if (userId != null) {
            DatabaseReference userQuizRef = usersRef.child(userId).child("quizzes").child(String.valueOf(quizId));
            userQuizRef.addValueEventListener(listener);
        }
    }
}