package com.example.green_action;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private FirebaseClient firebaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // FirebaseClient 초기화
        firebaseClient = new FirebaseClient();

        // UI 요소 참조
        ImageView profileImageView = findViewById(R.id.imageView);
        TextView profileIdTextView = findViewById(R.id.textView);
        TextView profileInfoTextView = findViewById(R.id.textView2);

        // Intent에서 사용자 ID를 가져오기
        String userId = getIntent().getStringExtra("user_uid");

        if (userId == null) {
            Log.e(TAG, "User ID is null");
            Toast.makeText(ProfileActivity.this, "사용자 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 사용자 데이터 읽기
        firebaseClient.loadUserData(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // 사용자 정보 UI에 설정
                    profileIdTextView.setText(user.id);
                    profileInfoTextView.setText("점수: " + user.score);

                    // 프로필 이미지 로드
                    if (user.profileImage != null && !user.profileImage.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(user.profileImage)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(profileImageView);
                    } else {
                        Log.w(TAG, "Profile image URL is null or empty");
                        // 프로필 이미지가 없을 때 처리 (예: 기본 이미지 설정)
                        profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } else {
                    Log.e(TAG, "User data is null");
                    Toast.makeText(ProfileActivity.this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(ProfileActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}