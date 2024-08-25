package com.example.green_action.air_pollution;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.green_action.DataBaseHandler;
import com.example.green_action.R;
import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AirQuizSectionFragment extends Fragment {

    private int quizNumber;
    private TextView textView;
    private EditText editTextAnswer;
    private Button buttonSubmitQuiz;
    private DataBaseHandler dbHandler;
    private FirebaseClient firebaseClient;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air_pollution_quiz_section, container, false);
        textView = view.findViewById(R.id.quizTextView);
        editTextAnswer = view.findViewById(R.id.answerEditText);
        buttonSubmitQuiz = view.findViewById(R.id.submitButton);

        dbHandler = new DataBaseHandler(getContext());
        firebaseClient = new FirebaseClient();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        if (getArguments() != null) {
            quizNumber = getArguments().getInt("QUIZ_NUMBER");
            textView.setText("퀴즈 " + quizNumber + " 문제입니다.");
        }

        buttonSubmitQuiz.setOnClickListener(v -> submitQuiz());

        return view;
    }

    private void submitQuiz() {
        String userAnswer = editTextAnswer.getText().toString().trim();
        String correctAnswer = getCorrectAnswerForQuiz(quizNumber);

        boolean isCorrectAnswer = userAnswer.equalsIgnoreCase(correctAnswer);

        if (isCorrectAnswer) {
            // Update quiz progress
            if (userId != null) {
                // Update progress in Firebase
                firebaseClient.saveQuizProgress(userId, quizNumber, true);
            }
            dbHandler.updateQuizStatus(quizNumber, true);

            // Notify AirQuizListFragment that the quiz is correct
            Bundle result = new Bundle();
            result.putInt("UNLOCK_QUIZ_NUMBER", quizNumber + 1);
            getParentFragmentManager().setFragmentResult("quiz_result", result);

            // Go back to AirQuizListFragment
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack(); // Go back to AirQuizStudyFragment
            fragmentManager.popBackStack(); // Now go back to AirQuizListFragment
        } else {
            // Show feedback to the user
            editTextAnswer.setError("정답이 아닙니다. 다시 시도하세요.");
        }
    }

    private String getCorrectAnswerForQuiz(int quizNumber) {
        switch (quizNumber) {
            case 1: return "정답1";
            case 2: return "정답2";
            case 3: return "정답3";
            case 4: return "정답4";
            case 5: return "정답5";
            case 6: return "정답6";
            case 7: return "정답7";
            case 8: return "정답8";
            case 9: return "정답9";
            case 10: return "정답10";
            case 30: return "정답30";
            default: return "";
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
                    fragmentManager.popBackStack(); // Go back to the previous fragment
                } else {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed(); // Default back action
                }
            }
        });
    }
}