package com.example.green_action.water_pollution;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.green_action.QuizDetail;
import com.example.green_action.R;
import com.example.green_action.air_pollution.AirQuizSectionFragment;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class WaterQuizStudyFragment extends Fragment {

    private static final String TAG = "WaterQuizStudyFragment";

    private int quizNumber;
    private String pollutionType; // 오염 유형을 저장할 변수
    private TextView textView;
    private FirebaseClient firebaseClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_water_pollution_quiz_study, container, false);
        textView = view.findViewById(R.id.textViewWaterPollution);
        Button buttonStartQuiz = view.findViewById(R.id.buttonQuizAndLearn);

        // Initialize FirebaseClient
        firebaseClient = new FirebaseClient();

        if (getArguments() != null) {
            quizNumber = getArguments().getInt("QUIZ_NUMBER", -1); // 기본값으로 -1을 설정
            pollutionType = getArguments().getString("POLLUTION_TYPE", "water_pollution"); // 기본값으로 air_pollution을 설정

            if (quizNumber != -1 && pollutionType != null) {
                loadStudyContent(pollutionType, quizNumber); // Load study content
            } else {
                Log.e(TAG, "Invalid quizNumber or pollutionType");
                textView.setText("Invalid quiz data.");
            }
        } else {
            Log.e(TAG, "No arguments passed to fragment");
            textView.setText("No quiz data available.");
        }

        buttonStartQuiz.setOnClickListener(v -> checkQuizStatusAndStart());

        return view;
    }

    private void loadStudyContent(String pollutionType, int quizNumber) {
        String quizId = String.valueOf(quizNumber); // Assuming quizNumber corresponds to quizId
        Log.d(TAG, "Loading quiz detail for pollutionType: " + pollutionType + ", quiz ID: " + quizId);

        firebaseClient.loadQuizDetail(pollutionType, quizId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    QuizDetail quizDetail = snapshot.getValue(QuizDetail.class);
                    if (quizDetail != null) {
                        String studyContent = quizDetail.getStudy();
                        textView.setText(studyContent);
                    } else {
                        textView.setText("No study content available.");
                    }
                } else {
                    textView.setText("No data found for this quiz.");
                    Log.d(TAG, "No data found for quiz ID: " + quizId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textView.setText("Failed to load study content.");
                Log.e(TAG, "Failed to load study content.", error.toException());
            }
        });
    }

    private void checkQuizStatusAndStart() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        firebaseClient.loadQuizProgress(userId, pollutionType, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer lastSolvedQuiz = snapshot.getValue(Integer.class);
                if (lastSolvedQuiz != null && quizNumber <= lastSolvedQuiz) {
                    Toast.makeText(getContext(), "이미 푼 문제입니다", Toast.LENGTH_SHORT).show();
                } else {
                    startQuiz();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check quiz progress", error.toException());
                Toast.makeText(getContext(), "문제 진행 상황을 확인하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startQuiz() {
        WaterQuizSectionFragment sectionFragment = new WaterQuizSectionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("QUIZ_NUMBER", quizNumber);
        bundle.putString("POLLUTION_TYPE", pollutionType); // 오염 유형 전달
        sectionFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, sectionFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getParentFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack(); // 이전 프래그먼트로 돌아가기
                } else {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed(); // 기본 뒤로가기 동작
                }
            }
        });
    }
}