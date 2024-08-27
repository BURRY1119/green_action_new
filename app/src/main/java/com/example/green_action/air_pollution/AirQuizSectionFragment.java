package com.example.green_action.air_pollution;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.green_action.QuizDetail;
import com.example.green_action.QuizViewModel;
import com.example.green_action.R;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AirQuizSectionFragment extends Fragment {

    private static final String TAG = "AirQuizSectionFragment";

    private TextView quizTextView;
    private TextView scoreTextView;
    private EditText answerEditText;
    private Button actionButton;
    private FirebaseClient firebaseClient;
    private String userId;
    private int quizId;
    private String pollutionType;
    private QuizViewModel quizViewModel;

    public AirQuizSectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_air_pollution_quiz_section, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        quizTextView = view.findViewById(R.id.quizTextView);
        scoreTextView = view.findViewById(R.id.scoreTextView);
        answerEditText = view.findViewById(R.id.answerEditText);
        actionButton = view.findViewById(R.id.submitButton);

        firebaseClient = new FirebaseClient();
        quizViewModel = new ViewModelProvider(this).get(QuizViewModel.class);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        if (getArguments() != null) {
            quizId = getArguments().getInt("QUIZ_NUMBER", 0);
            pollutionType = getArguments().getString("POLLUTION_TYPE", "air_pollution");
        }

        // ViewModel에서 초기 값 복원
        loadQuizStateFromFirebase();
        loadQuiz();

        actionButton.setOnClickListener(v -> handleButtonClick());
    }

    private void loadQuiz() {
        firebaseClient.loadQuizDetail(pollutionType, String.valueOf(quizId), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                QuizDetail quizDetail = dataSnapshot.getValue(QuizDetail.class);
                if (quizDetail != null) {
                    quizTextView.setText(quizDetail.getQuestion());

                    // ViewModel의 상태를 업데이트
                    if (quizViewModel.getCurrentMaxScore() == 0) {
                        quizViewModel.setCurrentMaxScore(quizDetail.getMaxScore());
                    }
                    if (quizViewModel.getAttemptsLeft() == 0) {
                        quizViewModel.setAttemptsLeft(quizDetail.getAttemptsAllowed());
                    }

                    // 화면에 표시
                    scoreTextView.setText("(" + quizViewModel.getCurrentMaxScore() + "점)");
                } else {
                    Toast.makeText(getContext(), "퀴즈 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "퀴즈 데이터를 불러오는 중 오류 발생", databaseError.toException());
            }
        });
    }

    private void handleButtonClick() {
        if (quizViewModel.getAttemptsLeft() > 0) {
            submitAnswer();
        } else {
            navigateToAirQuizListFragment();
        }
    }

    private void submitAnswer() {
        String userAnswer = answerEditText.getText().toString().trim();
        if (TextUtils.isEmpty(userAnswer)) {
            Toast.makeText(getContext(), "정답을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseClient.loadQuizDetail(pollutionType, String.valueOf(quizId), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                QuizDetail quizDetail = dataSnapshot.getValue(QuizDetail.class);
                if (quizDetail != null) {
                    if (quizDetail.checkAnswer(userAnswer)) {
                        int scoreToAdd = quizViewModel.getCurrentMaxScore();
                        Toast.makeText(getContext(), "정답입니다! " + "+" + scoreToAdd + "점", Toast.LENGTH_SHORT).show();
                        saveUserScore(scoreToAdd);
                        saveQuizProgress();
                        getParentFragmentManager().setFragmentResult("quiz_result", createResultBundle(quizId));
                        navigateToAirQuizListFragment();
                    } else {
                        updateQuizOnWrongAnswer(quizDetail);
                    }
                } else {
                    Log.e(TAG, "QuizDetail is null for quizId: " + quizId);
                    Toast.makeText(getContext(), "퀴즈 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load quiz data", databaseError.toException());
                Toast.makeText(getContext(), "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuizOnWrongAnswer(QuizDetail quizDetail) {
        int attemptsLeft = quizViewModel.getAttemptsLeft() - 1;
        int newMaxScore = quizViewModel.getCurrentMaxScore() - 1;

        quizViewModel.setAttemptsLeft(attemptsLeft);
        quizViewModel.setCurrentMaxScore(newMaxScore);

        saveQuizStateToFirebase();

        scoreTextView.setText("(" + newMaxScore + "점)");

        if (attemptsLeft <= 0) {
            // 정답과 해설을 표시하고 버튼을 '돌아가기'로 변경
            quizTextView.setText("정답: " + quizDetail.getAnswer() + "\n해설: " + quizDetail.getExplanation());
            actionButton.setText("돌아가기");
        } else {
            Toast.makeText(getContext(), "오답입니다. 남은 시도 횟수: " + attemptsLeft + "\n감점 후 점수: " + newMaxScore, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserScore(int scoreToAdd) {
        if (userId != null) {
            firebaseClient.loadUserData(userId, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer currentScore = snapshot.child("score").getValue(Integer.class);
                    if (currentScore != null) {
                        int newScore = currentScore + scoreToAdd;
                        snapshot.getRef().child("score").setValue(newScore);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load user data", error.toException());
                }
            });
        }
    }

    private void saveQuizProgress() {
        if (userId != null) {
            firebaseClient.saveQuizProgress(userId, pollutionType, quizId);
        }
    }

    private Bundle createResultBundle(int solvedQuizNumber) {
        Bundle result = new Bundle();
        result.putInt("SOLVED_QUIZ_NUMBER", solvedQuizNumber);
        result.putBoolean("IS_CORRECT", true);
        return result;
    }

    private void navigateToAirQuizListFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, new AirQuizListFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void saveQuizStateToFirebase() {
        if (userId != null) {
            firebaseClient.saveQuizState(userId, quizId, quizViewModel.getCurrentMaxScore(), quizViewModel.getAttemptsLeft());
        }
    }

    private void loadQuizStateFromFirebase() {
        if (userId != null) {
            firebaseClient.loadQuizState(userId, quizId, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer savedMaxScore = snapshot.child("maxScore").getValue(Integer.class);
                    Integer savedAttemptsLeft = snapshot.child("attemptsLeft").getValue(Integer.class);

                    if (savedMaxScore != null) {
                        quizViewModel.setCurrentMaxScore(savedMaxScore);
                    }
                    if (savedAttemptsLeft != null) {
                        quizViewModel.setAttemptsLeft(savedAttemptsLeft);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load quiz state", error.toException());
                }
            });
        }
    }
}