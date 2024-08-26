package com.example.green_action.Community;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.green_action.R;
import com.example.green_action.remote.FirebaseClient;

public class EditCommentActivity extends AppCompatActivity {
    private FirebaseClient firebaseClient;
    private String commentId, postId;
    private EditText editComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);

        ImageButton buttonback = findViewById(R.id.backButton);
        buttonback.setOnClickListener(v -> finish());

        firebaseClient = new FirebaseClient();

        editComment = findViewById(R.id.edit_comment_text);
        Button saveButton = findViewById(R.id.save_button);
        Button deleteButton = findViewById(R.id.delete_button);

        Intent intent = getIntent();
        commentId = intent.getStringExtra("commentId");
        postId = intent.getStringExtra("postId");
        String commentText = intent.getStringExtra("commentText");

        editComment.setText(commentText);

        saveButton.setOnClickListener(v -> {
            String newCommentText = editComment.getText().toString().trim();

            if (!newCommentText.isEmpty()) {
                firebaseClient.getCommentsRef(postId).child(commentId).child("commentText").setValue(newCommentText)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditCommentActivity.this, "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditCommentActivity.this, "댓글 수정 실패", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(EditCommentActivity.this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            firebaseClient.getCommentsRef(postId).child(commentId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditCommentActivity.this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditCommentActivity.this, "댓글 삭제 실패", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
