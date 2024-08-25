package com.example.green_action.air_pollution;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.green_action.DataBaseHandler;
import com.example.green_action.R;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AirQuizListFragment extends Fragment {

    private static final int NUM_COLUMNS = 5;  // Define the number of columns
    private static final int NUM_ROWS = 6;     // Define the number of rows
    private static final String TAG = "AirQuizListFragment"; // For logging purposes
    private GridLayout gridLayout;
    private FirebaseClient firebaseClient;
    private DataBaseHandler dbHandler;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air_pollution_quiz_list, container, false);

        gridLayout = view.findViewById(R.id.gridLayout);
        gridLayout.setColumnCount(NUM_COLUMNS);
        gridLayout.setRowCount(NUM_ROWS);

        firebaseClient = new FirebaseClient();
        dbHandler = new DataBaseHandler(getContext());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        setupQuizButtons();

        // Register FragmentResultListener to handle results from AirQuizSectionFragment
        getParentFragmentManager().setFragmentResultListener("quiz_result", getViewLifecycleOwner(), (requestKey, result) -> {
            int solvedQuizNumber = result.getInt("SOLVED_QUIZ_NUMBER");
            Log.d(TAG, "Received solved quiz number: " + solvedQuizNumber);
            updateQuizStatesAfterSolving(solvedQuizNumber);
        });

        // Load quiz progress from Firebase and local database
        loadQuizProgress();

        return view;
    }

    private void setupQuizButtons() {
        for (int i = 1; i <= NUM_COLUMNS * NUM_ROWS; i++) {
            Button button = new Button(getActivity());
            button.setText(String.valueOf(i));

            // 버튼 상태 설정
            boolean isSolved = dbHandler.getQuizStatus(i); // 로컬 데이터베이스에서 푼 상태 가져오기

            if (i == 1) {
                // 첫 번째 퀴즈의 경우
                if (isSolved) {
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_check, 0, 0); // 체크 아이콘
                    button.setEnabled(true);
                } else {
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_lock, 0, 0); // 잠금 아이콘
                    button.setEnabled(false);
                }
            } else {
                // 다른 퀴즈의 경우
                if (isSolved) {
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_check, 0, 0); // 체크 아이콘
                    button.setEnabled(true);
                } else {
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_lock, 0, 0); // 잠금 아이콘
                    button.setEnabled(false);
                }
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(4, 4, 4, 4);
            button.setBackgroundColor(getResources().getColor(R.color.airPollution));
            button.setTextColor(getResources().getColor(R.color.black));
            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                int quizNumber = Integer.parseInt(button.getText().toString());
                Log.d(TAG, "Button clicked for quiz number: " + quizNumber);
                handleButtonClick(quizNumber);
            });

            gridLayout.addView(button);
        }
    }

    private void handleButtonClick(int quizNumber) {
        Log.d(TAG, "Handling button click for quiz number: " + quizNumber);
        AirQuizStudyFragment studyFragment = new AirQuizStudyFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("QUIZ_NUMBER", quizNumber);
        studyFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, studyFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadQuizProgress() {
        if (userId != null) {
            Log.d(TAG, "Loading quiz progress for user: " + userId);
            firebaseClient.loadAllQuizProgress(userId, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "DataSnapshot received: " + dataSnapshot);
                    int lastSolvedQuiz = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        int quizId = Integer.parseInt(snapshot.getKey());
                        boolean isSolved = snapshot.getValue(Integer.class) == 1;
                        Log.d(TAG, "Quiz ID: " + quizId + ", Solved: " + isSolved);
                        dbHandler.addOrUpdateQuizProgress(quizId, isSolved); // Update local database

                        if (isSolved) {
                            lastSolvedQuiz = quizId; // 마지막으로 풀린 퀴즈 번호 업데이트
                        }
                    }

                    // Update button states based on the progress loaded
                    updateButtonStates(lastSolvedQuiz);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "DatabaseError: " + databaseError.getMessage());
                }
            });
        }
    }

    private void updateButtonStates(int lastSolvedQuiz) {
        for (int i = 1; i <= NUM_COLUMNS * NUM_ROWS; i++) {
            if (i <= lastSolvedQuiz) {
                // 이미 푼 퀴즈는 체크 아이콘으로
                updateButtonState(i, true);
            } else if (i == lastSolvedQuiz + 1) {
                // 다음 풀 수 있는 퀴즈는 잠금 해제 아이콘으로
                updateButtonState(i, false);
            } else {
                // 이후 퀴즈는 잠금 아이콘으로
                updateButtonState(i, false);
            }
        }
    }

    private void updateButtonState(int quizNumber, boolean isSolved) {
        Log.d(TAG, "Updating button state for quiz number: " + quizNumber + ", Solved: " + isSolved);
        Button button = (Button) gridLayout.getChildAt(quizNumber - 1); // 0-based index
        if (button != null) {
            if (isSolved) {
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_check, 0, 0); // 체크 아이콘 추가
                button.setEnabled(true);
            } else {
                if (quizNumber == 1 || quizNumber <= dbHandler.getLastSolvedQuiz() + 1) {
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_unlock, 0, 0); // 잠금 해제 아이콘 추가
                    button.setEnabled(true);
                } else {
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_lock, 0, 0); // 잠금 아이콘 추가
                    button.setEnabled(false);
                }
            }
        }
    }

    public void updateQuizStatesAfterSolving(int solvedQuizNumber) {
        Log.d(TAG, "Updating quiz states after solving quiz number: " + solvedQuizNumber);
        if (solvedQuizNumber < NUM_COLUMNS * NUM_ROWS) {
            Button button = (Button) gridLayout.getChildAt(solvedQuizNumber - 1); // 0-based index
            if (button != null) {
                // Update the solved quiz to a check icon
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_check, 0, 0); // 체크 아이콘 추가
                button.setEnabled(true);

                // Unlock the next quiz
                if (solvedQuizNumber + 1 <= NUM_COLUMNS * NUM_ROWS) {
                    Button nextButton = (Button) gridLayout.getChildAt(solvedQuizNumber); // 0-based index
                    if (nextButton != null) {
                        nextButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_unlock, 0, 0); // 잠금 해제 아이콘 추가
                        nextButton.setEnabled(true);
                    }
                }

                // Update the state of all subsequent quizzes to locked
                for (int i = solvedQuizNumber + 2; i <= NUM_COLUMNS * NUM_ROWS; i++) {
                    Button subsequentButton = (Button) gridLayout.getChildAt(i - 1); // 0-based index
                    if (subsequentButton != null) {
                        subsequentButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_lock, 0, 0); // 잠금 아이콘 추가
                        subsequentButton.setEnabled(false);
                    }
                }

                // Save progress to Firebase and local database
                if (userId != null) {
                    firebaseClient.saveQuizProgress(userId, solvedQuizNumber, true); // 퀴즈가 풀렸음으로 저장
                    dbHandler.addOrUpdateQuizProgress(solvedQuizNumber, true);
                }
            }
        }
    }

    private boolean isQuizSolved(int quizNumber) {
        boolean solved = dbHandler.getQuizStatus(quizNumber); // 로컬 DB에서 해당 퀴즈의 상태를 확인
        Log.d(TAG, "Checking if quiz number " + quizNumber + " is solved: " + solved);
        return solved;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getParentFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    Log.d(TAG, "Popping back stack");
                    fragmentManager.popBackStack(); // Go back to the previous fragment
                } else {
                    Log.d(TAG, "Default back action");
                    requireActivity().getOnBackPressedDispatcher().onBackPressed(); // Default back action
                }
            }
        });
    }
}