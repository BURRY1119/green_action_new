package com.example.green_action;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private LinearLayout leaderboardLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        leaderboardLayout = view.findViewById(R.id.leaderboard_layout);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchLeaderboardData(); // 프래그먼트가 다시 열릴 때마다 데이터를 가져와서 등수를 매깁니다.
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getParentFragmentManager();
                Fragment homeFragment = new HomeFragment();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, homeFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void fetchLeaderboardData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> userList = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.child("id").getValue(String.class);
                    Long score = userSnapshot.child("score").getValue(Long.class);
                    String profileImage = userSnapshot.child("profileImage").getValue(String.class);
                    if (userId != null && score != null) {
                        userList.add(new User(userSnapshot.getKey(), userId, score, profileImage));
                    }
                }

                // 사용자들의 score를 기준으로 내림차순 정렬
                Collections.sort(userList, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        return u2.getScore().compareTo(u1.getScore());
                    }
                });

                // 순위 계산 및 Firebase에 업데이트
                int rank = 1;
                Long previousScore = -1L;
                for (int i = 0; i < userList.size(); i++) {
                    User user = userList.get(i);

                    // 점수가 이전 사용자와 다를 경우에만 순위를 갱신
                    if (!user.getScore().equals(previousScore)) {
                        rank = i + 1;
                    }
                    previousScore = user.getScore();

                    // 순위를 Firebase에 저장
                    usersRef.child(user.getUid()).child("rank").setValue(rank);
                }

                // 상위 30명의 사용자 데이터를 UI에 표시
                displayTopUsers(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 에러 처리
            }
        });
    }

    private void displayTopUsers(List<User> userList) {
        leaderboardLayout.removeAllViews();  // 기존의 리더보드 데이터를 지우고 새 데이터를 추가합니다.

        int maxUsers = Math.min(30, userList.size());
        Long previousScore = -1L;
        int rank = 1;

        for (int i = 0; i < maxUsers; i++) {
            User user = userList.get(i);

            // 점수가 이전 사용자와 다를 경우에만 순위를 갱신
            if (!user.getScore().equals(previousScore)) {
                rank = i + 1;
            }
            previousScore = user.getScore();

            // 리더보드 항목을 위한 레이아웃 생성
            LinearLayout entryLayout = new LinearLayout(getContext());
            entryLayout.setOrientation(LinearLayout.HORIZONTAL);

            // 순위 TextView
            TextView rankTextView = new TextView(getContext());
            rankTextView.setText(String.valueOf(rank));
            rankTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));
            rankTextView.setTextSize(20);
            rankTextView.setTypeface(null, android.graphics.Typeface.BOLD);
            rankTextView.setTextColor(getResources().getColor(R.color.black));

            // 프로필 이미지 ImageView
            ImageView profileImageView = new ImageView(getContext());
            profileImageView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));
            Glide.with(this)
                    .load(user.getProfileImage())
                    .override(100, 100)
                    .circleCrop()
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImageView);

            // User ID TextView (12자 제한)
            String userId = user.getId().length() > 12 ? user.getId().substring(0, 12) : user.getId();
            TextView userIdTextView = new TextView(getContext());
            userIdTextView.setText(userId);
            userIdTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3f));
            userIdTextView.setTextSize(16);
            userIdTextView.setTextColor(getResources().getColor(R.color.black));

            // 점수 TextView
            TextView scoreTextView = new TextView(getContext());
            scoreTextView.setText(String.valueOf(user.getScore()));
            scoreTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            scoreTextView.setTextSize(24);
            scoreTextView.setTypeface(null, android.graphics.Typeface.BOLD);
            scoreTextView.setTextColor(getResources().getColor(R.color.black));
            scoreTextView.setGravity(View.TEXT_ALIGNMENT_VIEW_END);

            // 순위에 따른 배경색 설정
            if (rank == 1) {
                entryLayout.setBackgroundColor(getResources().getColor(R.color.gold));
            } else if (rank == 2) {
                entryLayout.setBackgroundColor(getResources().getColor(R.color.silver));
            } else if (rank == 3) {
                entryLayout.setBackgroundColor(getResources().getColor(R.color.bronze));
            } else if (rank >= 4 && rank <= 10) {
                entryLayout.setBackgroundColor(getResources().getColor(R.color.actionGreen));
            } else {
                entryLayout.setBackgroundColor(getResources().getColor(R.color.airPollution));
            }

            // TextViews 및 ImageView를 레이아웃에 추가
            entryLayout.addView(rankTextView);
            entryLayout.addView(profileImageView);
            entryLayout.addView(userIdTextView);
            entryLayout.addView(scoreTextView);

            // 패딩 및 마진 설정
            entryLayout.setPadding(32, 32, 32, 32);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 8, 0, 8);
            entryLayout.setLayoutParams(layoutParams);

            // 리더보드 레이아웃에 항목 추가
            leaderboardLayout.addView(entryLayout);
        }
    }

    private static class User {
        private String uid;
        private String id;
        private Long score;
        private String profileImage;

        public User(String uid, String id, Long score, String profileImage) {
            this.uid = uid;
            this.id = id;
            this.score = score;
            this.profileImage = profileImage;
        }

        public String getUid() {
            return uid;
        }

        public String getId() {
            return id;
        }

        public Long getScore() {
            return score;
        }

        public String getProfileImage() {
            return profileImage;
        }
    }
}