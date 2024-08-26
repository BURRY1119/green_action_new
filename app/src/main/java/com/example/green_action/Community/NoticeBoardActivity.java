package com.example.green_action.Community;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.green_action.DataBaseHandler;
import com.example.green_action.LoginActivity;
import com.example.green_action.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoticeBoardActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EDIT_POST = 1;

    private DataBaseHandler db_handler;
    private List<CommunityPostItem> postItemList;
    private RecyclerView recyclerView;
    private CommunityPostAdapter adapter;
    private String loggedInUserId;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board);

        ImageButton buttonback = findViewById(R.id.backButton);
        buttonback.setOnClickListener(v -> finish());

        db_handler = new DataBaseHandler(this);
        firebaseAuth = FirebaseAuth.getInstance();
        postItemList = new ArrayList<>();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            loggedInUserId = currentUser.getUid();
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            loggedInUserId = sharedPreferences.getString("loggedInUserId", "");
        }

        recyclerView = findViewById(R.id.community_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommunityPostAdapter(postItemList, this, loggedInUserId);
        recyclerView.setAdapter(adapter);

        Button writePostButton = findViewById(R.id.write_post_button);
        writePostButton.setOnClickListener(v -> {
            if (isLoggedIn()) {
                Intent intent = new Intent(NoticeBoardActivity.this, WritePostActivity.class);
                intent.putExtra("boardType", "notice");
                startActivity(intent);
            } else {
                Intent intent = new Intent(NoticeBoardActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(NoticeBoardActivity.this, "로그인 후 글을 작성할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        loadPosts();
    }

    private void loadPosts() {
        db_handler.getPostsByType("notice", posts -> {
            postItemList.clear();
            postItemList.addAll(posts);
            Collections.reverse(postItemList);
            adapter.notifyDataSetChanged();
        });
    }

    private boolean isLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null || !loggedInUserId.isEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_POST && resultCode == RESULT_OK) {
            if (data != null) {
                String postId = data.getStringExtra("postId");
                String updatedTitle = data.getStringExtra("title");
                String updatedContent = data.getStringExtra("content");
                long updatedTimestamp = data.getLongExtra("timestamp", -1);

                for (CommunityPostItem post : postItemList) {
                    if (post.getPostId().equals(postId)) {
                        post.setTitle(updatedTitle);
                        post.setContent(updatedContent);
                        post.setTimestamp(String.valueOf(updatedTimestamp));
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }
}
