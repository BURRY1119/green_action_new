package com.example.green_action.Community;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.green_action.LoginActivity;
import com.example.green_action.R;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    private FirebaseClient firebaseClient;
    private String postId, userId, title, content, timestamp, boardType;
    private RecyclerView commentRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private TextView likeCountTextView;
    private Button likeButton;
    private boolean liked = false;
    private FirebaseAuth firebaseAuth;
    private String loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            loggedInUserId = currentUser.getUid();
        }

        ImageButton buttonback = findViewById(R.id.backButton);
        buttonback.setOnClickListener(v -> finish());

        firebaseClient = new FirebaseClient();

        // Intent로부터 데이터 가져오기
        postId = getIntent().getStringExtra("postId");
        userId = getIntent().getStringExtra("userId");
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        timestamp = getIntent().getStringExtra("timestamp");
        boardType = getIntent().getStringExtra("boardType"); // boardType 추가

        // boardType 확인
        if (boardType == null || boardType.isEmpty()) {
            Toast.makeText(this, "게시판 유형을 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayPostDetails();

        // 좋아요 버튼과 카운트 초기화
        likeButton = findViewById(R.id.like_button);
        likeCountTextView = findViewById(R.id.like_count_text_view);
        checkIfUserLikedPost();

        likeButton.setOnClickListener(v -> {
            if (isLoggedIn()) {
                if (liked) {
                    decrementLikeCount();
                } else {
                    incrementLikeCount();
                }
            } else {
                Intent intent = new Intent(PostDetailActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(PostDetailActivity.this, "로그인 후 좋아요를 누를 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 댓글 RecyclerView 초기화
        commentRecyclerView = findViewById(R.id.comment_recycler_view);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        // 네 번째 매개변수로 boardType을 추가로 전달합니다.
        commentAdapter = new CommentAdapter(commentList, this, postId, boardType);
        commentRecyclerView.setAdapter(commentAdapter);

        // 댓글 데이터 불러오기
        loadComments();

        // 댓글 작성 버튼 처리
        EditText commentEditText = findViewById(R.id.comment_edit_text);
        Button submitCommentButton = findViewById(R.id.submit_comment_button);
        submitCommentButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                submitComment(commentText);
                commentEditText.setText(""); // 댓글 입력 필드 초기화
            } else {
                Toast.makeText(PostDetailActivity.this, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        submitCommentButton.setOnClickListener(v -> {
            if (isLoggedIn()) {
                String commentText = commentEditText.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    submitComment(commentText);
                    commentEditText.setText(""); // 댓글 입력 필드 초기화
                } else {
                    Toast.makeText(PostDetailActivity.this, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(PostDetailActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(PostDetailActivity.this, "로그인 후 댓글을 작성할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 더보기 버튼 처리
        ImageButton moreButton = findViewById(R.id.post_more_button);
        moreButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(PostDetailActivity.this, v);
            popupMenu.getMenuInflater().inflate(R.menu.post_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    if (isPostOwner()) {
                        Intent intent = new Intent(PostDetailActivity.this, EditPostActivity.class);
                        intent.putExtra("postId", postId);
                        intent.putExtra("userId", userId);
                        intent.putExtra("title", title);
                        intent.putExtra("content", content);
                        intent.putExtra("timestamp", timestamp);
                        intent.putExtra("boardType", boardType);
                        startActivity(intent);
                    } else {
                        Toast.makeText(PostDetailActivity.this, "수정 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    if (isPostOwner()) {
                        deletePost();
                    } else {
                        Toast.makeText(PostDetailActivity.this, "삭제 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private boolean isLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null || (loggedInUserId != null && !loggedInUserId.isEmpty());
    }

    private void displayPostDetails() {
        TextView titleTextView = findViewById(R.id.detail_post_title);
        TextView contentTextView = findViewById(R.id.detail_post_content);
        TextView timestampTextView = findViewById(R.id.detail_post_timestamp);
        TextView idTextView = findViewById(R.id.detail_post_username);

        titleTextView.setText(title);
        contentTextView.setText(content);
        timestampTextView.setText(convertTimestampToDate(timestamp));

        retrieveAndDisplayUsername(userId, idTextView);
    }

    private void retrieveAndDisplayUsername(String userId, TextView idTextView) {
        DatabaseReference userRef = firebaseClient.getUsersRef().child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("name").getValue(String.class);
                    if (username == null || username.isEmpty()) {
                        username = "Unknown User";
                    }
                    String displayId = userId.substring(0, Math.min(userId.length(), 7));
                    idTextView.setText(username + " (" + displayId + ")");
                } else {
                    String displayId = userId.substring(0, Math.min(userId.length(), 7));
                    idTextView.setText("Unknown User (" + displayId + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String displayId = userId.substring(0, Math.min(userId.length(), 7));
                idTextView.setText("Unknown User (" + displayId + ")");
                Toast.makeText(PostDetailActivity.this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfUserLikedPost() {
        DatabaseReference userLikeRef = firebaseClient.getPostsRef(boardType).child(postId).child("userLikes").child(getUserId());
        userLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                liked = snapshot.exists();
                updateLikeButtonState();
                loadLikeCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "좋아요 상태를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLikeCount() {
        DatabaseReference likesRef = firebaseClient.getPostsRef(boardType).child(postId).child("likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int likeCount = 0;
                if (snapshot.exists()) {
                    likeCount = snapshot.getValue(Integer.class);
                }
                likeCountTextView.setText(String.valueOf(likeCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "좋아요 수를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void incrementLikeCount() {
        DatabaseReference postRef = firebaseClient.getPostsRef(boardType).child(postId);
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentValue = currentData.child("likes").getValue(Integer.class);
                if (currentValue == null) {
                    currentData.child("likes").setValue(1);
                } else {
                    currentData.child("likes").setValue(currentValue + 1);
                }
                currentData.child("userLikes").child(getUserId()).setValue(true);
                liked = true;
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@NonNull DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    Toast.makeText(PostDetailActivity.this, "좋아요 증가 실패", Toast.LENGTH_SHORT).show();
                } else {
                    updateLikeButtonState();
                }
            }
        });
    }

    private void decrementLikeCount() {
        DatabaseReference postRef = firebaseClient.getPostsRef(boardType).child(postId);
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentValue = currentData.child("likes").getValue(Integer.class);
                if (currentValue != null && currentValue > 0) {
                    currentData.child("likes").setValue(currentValue - 1);
                }
                currentData.child("userLikes").child(getUserId()).setValue(null);
                liked = false;
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@NonNull DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    Toast.makeText(PostDetailActivity.this, "좋아요 감소 실패", Toast.LENGTH_SHORT).show();
                } else {
                    updateLikeButtonState();
                }
            }
        });
    }

    private void updateLikeButtonState() {
        if (liked) {
            likeButton.setBackgroundResource(R.drawable.pinkheart);
            likeButton.setAlpha(0.5f);
        } else {
            likeButton.setBackgroundResource(R.drawable.pinkheart);
            likeButton.setAlpha(1.0f);
        }
    }

    private void loadComments() {
        DatabaseReference commentsRef = firebaseClient.getPostsRef(boardType).child(postId).child("comments");
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "댓글을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitComment(String commentText) {
        DatabaseReference commentsRef = firebaseClient.getPostsRef(boardType).child(postId).child("comments").push();
        String commentId = commentsRef.getKey();
        String currentUserId = getUserId();
        String username = getLoggedInUsername();
        // Comment 생성 시 boardType을 포함하여 초기화합니다.
        Comment comment = new Comment(commentId, currentUserId, username, commentText, System.currentTimeMillis(), boardType);
        commentsRef.setValue(comment);
    }

    private boolean isPostOwner() {
        return getUserId().equals(userId);
    }

    private void deletePost() {
        firebaseClient.getPostsRef(boardType).child(postId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PostDetailActivity.this, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostDetailActivity.this, "게시물 삭제 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private String convertTimestampToDate(String timestamp) {
        long time = Long.parseLong(timestamp);
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

    private String getUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "unknown";
        }
    }

    private String getLoggedInUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getDisplayName() + " (" + currentUser.getUid().substring(0, 7) + ")";
        } else {
            return "Unknown User (unknown)";
        }
    }
}
