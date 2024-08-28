package com.example.green_action.Community;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.green_action.DataBaseHandler;
import com.example.green_action.R;

public class EditPostActivity extends AppCompatActivity {
    private DataBaseHandler db_handler;
    private String postId, boardType;
    private EditText editTitle, editContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        ImageButton buttonback = findViewById(R.id.backButton);
        buttonback.setOnClickListener(v -> finish());

        db_handler = new DataBaseHandler(this);

        editTitle = findViewById(R.id.edit_post_title);
        editContent = findViewById(R.id.edit_post_content);
        Button saveButton = findViewById(R.id.save_button);
        Button deleteButton = findViewById(R.id.delete_button);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        boardType = intent.getStringExtra("boardType"); // 게시판 유형을 받음
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");

        // boardType 검사 추가
        if (boardType == null || boardType.isEmpty()) {
            Toast.makeText(this, "게시판 유형을 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
            Log.e("EditPostActivity", "boardType is null or empty");
            finish();
            return;
        } else {
            Log.d("EditPostActivity", "Received boardType: " + boardType);
        }

        editTitle.setText(title);
        editContent.setText(content);

        saveButton.setOnClickListener(v -> {
            String newTitle = editTitle.getText().toString().trim();
            String newContent = editContent.getText().toString().trim();

            if (!newTitle.isEmpty() && !newContent.isEmpty()) {
                db_handler.updatePost(postId, newTitle, newContent, boardType); // 게시판 유형을 포함하여 업데이트
                Toast.makeText(EditPostActivity.this, "게시물이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditPostActivity.this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            db_handler.deletePost(postId, boardType); // 게시판 유형을 포함하여 삭제
            Toast.makeText(EditPostActivity.this, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}