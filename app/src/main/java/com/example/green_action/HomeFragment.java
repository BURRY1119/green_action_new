package com.example.green_action;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.green_action.Community.FreeBoardActivity;
import com.example.green_action.Community.IssueBoardActivity;
import com.example.green_action.Community.NoticeBoardActivity;
import com.example.green_action.Community.QnaBoardActivity;
import com.example.green_action.air_pollution.AirPollutionFragment;
import com.example.green_action.plastic_pollution.PlasticPollutionFragment;
import com.example.green_action.soil_pollution.SoilPollutionFragment;
import com.example.green_action.water_pollution.WaterPollutionFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private TextView issueBoardTitle, issueLatestPostTitle, freeBoardTitle, freeLatestPostTitle;
    private TextView noticeBoardTitle, noticeLatestPostTitle, qnaBoardTitle, qnaLatestPostTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 최신 게시물 제목과 게시판 이름을 표시할 TextView 초기화
        issueBoardTitle = view.findViewById(R.id.tv_issue_board);
        issueLatestPostTitle = view.findViewById(R.id.tv_issue_latest_post_title);
        freeBoardTitle = view.findViewById(R.id.tv_free_board);
        freeLatestPostTitle = view.findViewById(R.id.tv_free_latest_post_title);
        noticeBoardTitle = view.findViewById(R.id.tv_notice_board);
        noticeLatestPostTitle = view.findViewById(R.id.tv_notice_latest_post_title);
        qnaBoardTitle = view.findViewById(R.id.tv_qna_board);
        qnaLatestPostTitle = view.findViewById(R.id.tv_qna_latest_post_title);

        // 최신 게시물 제목 불러오기
        setupRealtimeUpdates("issue_posts", issueLatestPostTitle);
        setupRealtimeUpdates("free_posts", freeLatestPostTitle);
        setupRealtimeUpdates("notice_posts", noticeLatestPostTitle);
        setupRealtimeUpdates("qna_posts", qnaLatestPostTitle);

        // 버튼 클릭 이벤트 설정
        setButtonTouchListener(view.findViewById(R.id.air_pollution));
        setButtonTouchListener(view.findViewById(R.id.water_pollution));
        setButtonTouchListener(view.findViewById(R.id.soil_pollution));
        setButtonTouchListener(view.findViewById(R.id.plastic_pollution));

        // TextView 클릭 이벤트 설정: 각 게시판으로 이동 (게시판 이름과 최신 글 미리보기 모두 적용)
        setTextViewClickListener(issueBoardTitle, IssueBoardActivity.class);
        setTextViewClickListener(issueLatestPostTitle, IssueBoardActivity.class);
        setTextViewClickListener(freeBoardTitle, FreeBoardActivity.class);
        setTextViewClickListener(freeLatestPostTitle, FreeBoardActivity.class);
        setTextViewClickListener(noticeBoardTitle, NoticeBoardActivity.class);
        setTextViewClickListener(noticeLatestPostTitle, NoticeBoardActivity.class);
        setTextViewClickListener(qnaBoardTitle, QnaBoardActivity.class);
        setTextViewClickListener(qnaLatestPostTitle, QnaBoardActivity.class);

        return view;
    }

    // Firebase에서 최신 게시물 제목을 실시간으로 가져오는 메서드
    private void setupRealtimeUpdates(String postType, TextView textView) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference(postType);

        // 게시물을 타임스탬프 역순으로 정렬하고 첫 번째 항목을 가져옴
        Query latestPostQuery = postsRef.orderByChild("timestamp").limitToLast(1);

        latestPostQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String postTitle = postSnapshot.child("title").getValue(String.class);
                        if (postTitle != null) {
                            textView.setText(postTitle);
                        } else {
                            textView.setText("최신 글 제목을 불러올 수 없습니다.");
                        }
                    }
                } else {
                    textView.setText("최신 글이 없습니다.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                textView.setText("데이터베이스 오류: " + databaseError.getMessage());
            }
        });
    }

    // 터치 이벤트 설정
    private void setButtonTouchListener(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;
            private boolean isClick = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        isClick = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - startX) > 10 || Math.abs(event.getY() - startY) > 10) {
                            isClick = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isClick) {
                            onButtonClicked(v);
                        }
                        break;
                }
                return true;
            }
        });
    }

    // 버튼 클릭 이벤트 처리
    private void onButtonClicked(View view) {
        Fragment fragment = null;
        if (view.getId() == R.id.air_pollution) {
            fragment = new AirPollutionFragment();
        } else if (view.getId() == R.id.water_pollution) {
            fragment = new WaterPollutionFragment();
        } else if (view.getId() == R.id.soil_pollution) {
            fragment = new SoilPollutionFragment();
        } else if (view.getId() == R.id.plastic_pollution) {
            fragment = new PlasticPollutionFragment();
        }
        if (fragment != null) {
            replaceFragment(fragment);
        }
    }

    // Fragment를 교체하는 메서드
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null); // Back stack에 추가
        fragmentTransaction.commit();
    }

    // TextView 클릭 이벤트 설정
    private void setTextViewClickListener(TextView textView, Class<?> activityClass) {
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), activityClass);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 앱 종료
                requireActivity().finish();
            }
        });
    }
}
