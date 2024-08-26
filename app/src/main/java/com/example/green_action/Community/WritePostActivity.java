package com.example.green_action.Community;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.green_action.R;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WritePostActivity extends AppCompatActivity {

    private FirebaseClient firebaseClient;
    private DatabaseReference postsRef;
    private String boardType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        ImageButton buttonback = findViewById(R.id.backButton);
        buttonback.setOnClickListener(v -> finish());

        firebaseClient = new FirebaseClient();

        // 게시판 타입을 받아옴
        boardType = getIntent().getStringExtra("boardType");
        if (boardType == null) {
            Toast.makeText(this, "게시판 타입을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 게시판에 맞는 Firebase 경로 설정
        postsRef = FirebaseDatabase.getInstance().getReference(boardType + "_posts");

        EditText titleEditText = findViewById(R.id.post_title);
        EditText contentEditText = findViewById(R.id.post_content);
        Button submitButton = findViewById(R.id.button_submit_post);

        submitButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String content = contentEditText.getText().toString().trim();
            String userId = getUserId(); // 사용자 ID를 가져오는 메서드
            String username = getUserName(); // 사용자 이름을 가져오는 메서드

            if (!title.isEmpty() && !content.isEmpty()) {
                DatabaseReference newPostRef = postsRef.push(); // 고유 postId 생성
                newPostRef.setValue(new CommunityPostItem(newPostRef.getKey(), userId, title, content, getCurrentTimestamp(), username))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(WritePostActivity.this, "게시물이 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show();
                            // 작성 완료 후 원래 게시판으로 돌아가기
                            Intent intent = new Intent(WritePostActivity.this, getBoardActivityClass());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // 현재 액티비티 종료
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(WritePostActivity.this, "게시물 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(WritePostActivity.this, "모든 필드를 작성해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Class<?> getBoardActivityClass() {
        switch (boardType) {
            case "issue":
                return IssueBoardActivity.class;
            case "free":
                return FreeBoardActivity.class;
            case "notice":
                return NoticeBoardActivity.class;
            case "qna":
                return QnaBoardActivity.class;
            default:
                throw new IllegalArgumentException("Invalid board type");
        }
    }

    private String getUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            // 앱 자체 로그인 사용자라면 SharedPreferences에서 사용자 ID를 반환
            return getCustomUserId();
        }
    }

    private String getUserName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getDisplayName();
        } else {
            // 앱 자체 로그인 사용자의 이름을 반환
            return getCustomUsername();
        }
    }

    private String getCustomUserId() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("user_id", "unknown");
    }

    private String getCustomUsername() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("username", "Unknown User");
    }

    private String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
}
