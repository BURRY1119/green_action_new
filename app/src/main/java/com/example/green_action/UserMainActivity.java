package com.example.green_action;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

public class UserMainActivity extends AppCompatActivity {

    private static final String TAG = "UserMain";
    private FirebaseClient firebaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        // FirebaseClient 초기화
        firebaseClient = new FirebaseClient();

        // UI 요소 참조
        ImageView userProfileImage = findViewById(R.id.user_profile_image);
        TextView userIdText = findViewById(R.id.user_id_text);
        TextView userEmailText = findViewById(R.id.user_email_text);
        TextView userNameText = findViewById(R.id.user_name_text);
        TextView userContactText = findViewById(R.id.user_contact_text);
        Button updateInfoButton = findViewById(R.id.update_info_button);

        // Intent에서 사용자 ID를 가져오기
        String userId = getIntent().getStringExtra("user_uid");

        if (userId == null) {
            Log.e(TAG, "User ID is null");
            Toast.makeText(UserMainActivity.this, "사용자 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 사용자 데이터 읽기
        firebaseClient.loadUserData(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // 사용자 데이터 UI에 설정
                    userIdText.setText(user.id);
                    userEmailText.setText(user.email);
                    userNameText.setText(user.name);
                    userContactText.setText(user.contact);

                    // 프로필 이미지 로드
                    if (user.profileImage != null && !user.profileImage.isEmpty()) {
                        Glide.with(UserMainActivity.this)
                                .load(user.profileImage)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(userProfileImage);
                    } else {
                        Log.w(TAG, "Profile image URL is null or empty");
                    }
                } else {
                    Log.e(TAG, "User data is null");
                    Toast.makeText(UserMainActivity.this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(UserMainActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 정보 업데이트 버튼 클릭 리스너 설정
        updateInfoButton.setOnClickListener(v -> {
            // 정보 업데이트 로직 추가 (예: 정보 업데이트 화면으로 이동)
            Log.d(TAG, "Update info button clicked");
        });
    }
}