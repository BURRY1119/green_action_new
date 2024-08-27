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

import com.example.green_action.R;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AirQuizListFragment extends Fragment {

    private static final int NUM_COLUMNS = 5;
    private static final int NUM_ROWS = 6;
    private static final String TAG = "AirQuizListFragment";
    private GridLayout gridLayout;
    private FirebaseClient firebaseClient;
    private String userId;
    private String pollutionType = "air_pollution";  // 이 부분은 전달받거나 설정해야 할 부분
    private int lastSolvedQuiz = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air_pollution_quiz_list, container, false);

        gridLayout = view.findViewById(R.id.gridLayout);
        gridLayout.setColumnCount(NUM_COLUMNS);
        gridLayout.setRowCount(NUM_ROWS);

        firebaseClient = new FirebaseClient();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        setupQuizButtons();

        getParentFragmentManager().setFragmentResultListener("quiz_result", getViewLifecycleOwner(), (requestKey, result) -> {
            int solvedQuizNumber = result.getInt("SOLVED_QUIZ_NUMBER");
            boolean isCorrect = result.getBoolean("IS_CORRECT", false);
            Log.d(TAG, "Received solved quiz number: " + solvedQuizNumber + ", isCorrect: " + isCorrect);

            updateQuizStatesAfterSolving(solvedQuizNumber, isCorrect);
        });

        loadLastSolvedQuiz();

        return view;
    }

    private void setupQuizButtons() {
        for (int i = 1; i <= NUM_COLUMNS * NUM_ROWS; i++) {
            Button button = new Button(getActivity());
            button.setText(String.valueOf(i));

            button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_lock, 0, 0);
            button.setEnabled(false);

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
        bundle.putString("POLLUTION_TYPE", pollutionType);

        studyFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, studyFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadLastSolvedQuiz() {
        if (userId != null && pollutionType != null) {
            Log.d(TAG, "Loading last solved quiz for user: " + userId + " and pollution type: " + pollutionType);
            firebaseClient.loadQuizProgress(userId, pollutionType, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "DataSnapshot received: " + dataSnapshot);

                    if (dataSnapshot.exists()) {
                        lastSolvedQuiz = dataSnapshot.getValue(Integer.class);
                    }

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
                updateButtonState(i, true, false);
            } else {
                updateButtonState(i, false, i == lastSolvedQuiz + 1);
            }
        }
    }

    private void updateButtonState(int quizNumber, boolean isSolved, boolean isUnlockable) {
        Log.d(TAG, "Updating button state for quiz number: " + quizNumber + ", Solved: " + isSolved + ", Unlockable: " + isUnlockable);
        Button button = (Button) gridLayout.getChildAt(quizNumber - 1);
        if (button != null) {
            if (isSolved) {
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_check, 0, 0);
                button.setEnabled(true);
            } else {
                if (isUnlockable) {
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_unlock, 0, 0);
                    button.setEnabled(true);
                } else {
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_lock, 0, 0);
                    button.setEnabled(false);
                }
            }
        }
    }

    public void updateQuizStatesAfterSolving(int solvedQuizNumber, boolean isCorrect) {
        Log.d(TAG, "Updating quiz states after solving quiz number: " + solvedQuizNumber + ", isCorrect: " + isCorrect);

        if (isCorrect) {
            updateButtonState(solvedQuizNumber, true, false);

            if (solvedQuizNumber == lastSolvedQuiz + 1) {
                lastSolvedQuiz = solvedQuizNumber;

                if (solvedQuizNumber + 1 <= NUM_COLUMNS * NUM_ROWS) {
                    updateButtonState(solvedQuizNumber + 1, false, true);
                }
            }

            if (userId != null && pollutionType != null) {
                firebaseClient.saveQuizProgress(userId, pollutionType, lastSolvedQuiz);
            }
        } else {
            Log.d(TAG, "Answer is incorrect. No changes to the quiz states.");
        }
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
                    fragmentManager.popBackStack();
                } else {
                    Log.d(TAG, "Default back action");
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}