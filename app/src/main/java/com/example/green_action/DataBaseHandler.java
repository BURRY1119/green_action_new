package com.example.green_action;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.green_action.Community.CommunityPostItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quiz_database";
    private static final int DATABASE_VERSION = 1;

    // Firebase 관련 필드
    private final DatabaseReference usersRef;
    private final DatabaseReference issuePostsRef;
    private final DatabaseReference freePostsRef;    // 추가된 부분
    private final DatabaseReference noticePostsRef;  // 추가된 부분
    private final DatabaseReference qnaPostsRef;     // 추가된 부분

    // SQLite 관련 테이블 이름과 컬럼 정의
    private static final String TABLE_QUIZ_PROGRESS = "quiz_progress";
    private static final String COLUMN_QUIZ_ID = "quiz_id";
    private static final String COLUMN_QUIZ_STATUS = "is_solved"; // 0: 미해결, 1: 해결됨

    // 생성자
    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        issuePostsRef = database.getReference("issue_posts");
        freePostsRef = database.getReference("free_posts");     // 추가된 부분
        noticePostsRef = database.getReference("notice_posts"); // 추가된 부분
        qnaPostsRef = database.getReference("qna_posts");       // 추가된 부분
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 퀴즈 진행 상태 테이블 생성 쿼리
        String CREATE_QUIZ_PROGRESS_TABLE = "CREATE TABLE " + TABLE_QUIZ_PROGRESS + " (" +
                COLUMN_QUIZ_ID + " INTEGER PRIMARY KEY," +
                COLUMN_QUIZ_STATUS + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_QUIZ_PROGRESS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 기존 테이블 삭제 후 재생성
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_PROGRESS);
        onCreate(db);
    }

    // 퀴즈 진행 상태 추가/업데이트 메서드
    public long addOrUpdateQuizProgress(int quizId, boolean isSolved) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUIZ_ID, quizId);
        values.put(COLUMN_QUIZ_STATUS, isSolved ? 1 : 0);

        // 퀴즈 진행 상태를 업데이트하거나 삽입
        long result = db.replace(TABLE_QUIZ_PROGRESS, null, values);
        db.close(); // 데이터베이스 닫기
        return result;
    }

    // 퀴즈 진행 상태 업데이트 메서드
    public void updateQuizStatus(int quizId, boolean isSolved) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUIZ_STATUS, isSolved ? 1 : 0);

        int rowsUpdated = db.update(TABLE_QUIZ_PROGRESS, values, COLUMN_QUIZ_ID + "=?", new String[]{String.valueOf(quizId)});
        if (rowsUpdated == 0) {
            Log.e("DatabaseError", "Failed to update quiz status. No rows updated.");
        }
        db.close();
    }

    // 퀴즈 진행 상태 로드 메서드
    public boolean getQuizStatus(int quizId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_QUIZ_STATUS + " FROM " + TABLE_QUIZ_PROGRESS + " WHERE " + COLUMN_QUIZ_ID + "=?";
        Cursor cursor = null;
        boolean status = false;

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(quizId)});
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_QUIZ_STATUS);
                if (columnIndex != -1) {
                    int statusValue = cursor.getInt(columnIndex);
                    status = statusValue == 1;
                } else {
                    Log.e("DatabaseError", "Column '" + COLUMN_QUIZ_STATUS + "' not found");
                }
            } else {
                Log.e("DatabaseError", "No quiz progress found for quizId: " + quizId);
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error retrieving quiz status for quizId: " + quizId, e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Cursor를 닫습니다.
            }
            db.close(); // 데이터베이스 연결을 닫습니다.
        }

        return status;
    }

    // Firebase를 사용한 게시물 추가 함수
    public void addPost(CommunityPostItem post, String boardType) {
        DatabaseReference postsRef = getPostsReference(boardType);
        if (postsRef != null) {
            DatabaseReference newPostRef = postsRef.push();
            post.setPostId(newPostRef.getKey());
            newPostRef.setValue(post);
        }
    }

    // Firebase를 사용한 게시물 수정 함수
    public void updatePost(String postId, String title, String content, String boardType) {
        DatabaseReference postsRef = getPostsReference(boardType);
        if (postsRef != null) {
            postsRef.child(postId).child("title").setValue(title);
            postsRef.child(postId).child("content").setValue(content);
        }
    }

    // Firebase를 사용한 게시물 삭제 함수
    public void deletePost(String postId, String boardType) {
        DatabaseReference postsRef = getPostsReference(boardType);
        if (postsRef != null) {
            postsRef.child(postId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DataBaseHandler", "게시물이 성공적으로 삭제되었습니다: " + postId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DataBaseHandler", "게시물 삭제 실패: " + postId, e);
                    });
        } else {
            Log.e("DataBaseHandler", "잘못된 게시판 유형: " + boardType);
        }
    }


    // 특정 유형의 게시물 가져오기 함수
    public void getPostsByType(String boardType, final OnPostsRetrievedListener listener) {
        DatabaseReference postsRef = getPostsReference(boardType);
        if (postsRef != null) {
            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<CommunityPostItem> postList = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CommunityPostItem post = postSnapshot.getValue(CommunityPostItem.class);

                        if (post != null) {
                            // Log를 통해 username 필드가 올바르게 로드되는지 확인
                            Log.d("DataBaseHandler", "Retrieved post: " + post.getTitle() + ", User: " + post.getUserName());

                            postList.add(post);
                        }
                    }
                    listener.onRetrieved(postList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("DataBaseHandler", "Failed to retrieve posts", databaseError.toException());
                    listener.onRetrieved(new ArrayList<>());
                }
            });
        } else {
            Log.e("DataBaseHandler", "Invalid board type: " + boardType);
            listener.onRetrieved(new ArrayList<>());
        }
    }

    // 모든 게시물 가져오기 함수
    public void getAllPosts(final OnPostsRetrievedListener listener) {
        issuePostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CommunityPostItem> postList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CommunityPostItem post = postSnapshot.getValue(CommunityPostItem.class);
                    postList.add(post);
                }
                listener.onRetrieved(postList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onRetrieved(new ArrayList<>());
            }
        });
    }

    // 게시판 유형에 따른 참조 반환 메서드
    private DatabaseReference getPostsReference(String boardType) {
        switch (boardType) {
            case "issue":
                return issuePostsRef;
            case "free":
                return freePostsRef;
            case "notice":
                return noticePostsRef;
            case "qna":
                return qnaPostsRef;
            default:
                return null;
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

    // 인터페이스: 게시물 조회 결과를 위한 콜백
    public interface OnPostsRetrievedListener {
        void onRetrieved(List<CommunityPostItem> posts);
    }

    // 인터페이스: 중복 체크 결과를 위한 콜백
    public interface OnCheckUserExistsListener {
        void onCheck(boolean exists);
    }

    // 마지막으로 푼 퀴즈 번호 가져오기
    public int getLastSolvedQuiz() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MAX(" + COLUMN_QUIZ_ID + ") FROM " + TABLE_QUIZ_PROGRESS + " WHERE " + COLUMN_QUIZ_STATUS + "=1";
        Cursor cursor = null;
        int lastSolvedQuiz = 0;

        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("MAX(" + COLUMN_QUIZ_ID + ")");
                if (columnIndex != -1) {
                    lastSolvedQuiz = cursor.getInt(columnIndex);
                } else {
                    Log.e("DatabaseError", "Column 'MAX(" + COLUMN_QUIZ_ID + ")' not found");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error retrieving last solved quiz", e);
        } finally {
            if (cursor != null) {
                cursor.close(); // Cursor를 닫습니다.
            }
            db.close(); // 데이터베이스 연결을 닫습니다.
        }

        return lastSolvedQuiz;
    }
}
