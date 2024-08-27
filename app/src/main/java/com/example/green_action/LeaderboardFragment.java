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
                        userList.add(new User(userId, score, profileImage));
                    }
                }

                // Sort the list by score in descending order
                Collections.sort(userList, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        return u2.getScore().compareTo(u1.getScore());
                    }
                });

                // Display the top 30 users
                displayTopUsers(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void displayTopUsers(List<User> userList) {
        leaderboardLayout.removeAllViews();  // 기존의 리더보드 데이터를 지우고 새 데이터를 추가합니다.

        int maxUsers = Math.min(30, userList.size());
        Long previousScore = -1L;  // previousScore를 Long 타입으로 변경
        int rank = 1;

        for (int i = 0; i < maxUsers; i++) {
            User user = userList.get(i);

            // If score is different from the previous, update the rank
            if (!user.getScore().equals(previousScore)) {  // Long 타입이므로 equals()로 비교
                rank = i + 1;
            }
            previousScore = user.getScore();

            // Create a horizontal LinearLayout for each leaderboard entry
            LinearLayout entryLayout = new LinearLayout(getContext());
            entryLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Rank TextView
            TextView rankTextView = new TextView(getContext());
            rankTextView.setText(String.valueOf(rank));
            rankTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));
            rankTextView.setTextSize(20);
            rankTextView.setTypeface(null, android.graphics.Typeface.BOLD);
            rankTextView.setTextColor(getResources().getColor(R.color.black));

            // Profile ImageView
            ImageView profileImageView = new ImageView(getContext());
            profileImageView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f));
            Glide.with(this)
                    .load(user.getProfileImage())
                    .override(100, 100)  // 이미지 크기를 100x100 픽셀로 조정
                    .circleCrop()  // 이미지를 원형으로 표시합니다.
                    .error(R.drawable.ic_profile_placeholder) // 프로필 이미지가 없는 경우 기본 이미지 설정
                    .into(profileImageView);

            // User ID TextView (limit to 12 characters)
            String userId = user.getId().length() > 12 ? user.getId().substring(0, 12) : user.getId();
            TextView userIdTextView = new TextView(getContext());
            userIdTextView.setText(userId);
            userIdTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3f));
            userIdTextView.setTextSize(16);
            userIdTextView.setTextColor(getResources().getColor(R.color.black));

            // Score TextView
            TextView scoreTextView = new TextView(getContext());
            scoreTextView.setText(String.valueOf(user.getScore()));
            scoreTextView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            scoreTextView.setTextSize(24);
            scoreTextView.setTypeface(null, android.graphics.Typeface.BOLD); // 글씨 스타일을 볼드체로 설정
            scoreTextView.setTextColor(getResources().getColor(R.color.black));
            scoreTextView.setGravity(View.TEXT_ALIGNMENT_VIEW_END);  // 점수를 오른쪽 정렬

            // Apply background color based on rank
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

            // Add TextViews and ImageView to the entry layout
            entryLayout.addView(rankTextView);
            entryLayout.addView(profileImageView);  // 프로필 이미지 추가
            entryLayout.addView(userIdTextView);
            entryLayout.addView(scoreTextView);

            // Add padding and margins for better UI
            entryLayout.setPadding(32, 32, 32, 32);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 8, 0, 8);
            entryLayout.setLayoutParams(layoutParams);

            // Add the entry layout to the leaderboard layout
            leaderboardLayout.addView(entryLayout);
        }
    }

    private static class User {
        private String id;
        private Long score;
        private String profileImage;

        public User(String id, Long score, String profileImage) {
            this.id = id;
            this.score = score;
            this.profileImage = profileImage;
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