package com.example.green_action.plastic_pollution;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.green_action.R;

public class PlasticPollutionFragment extends Fragment {

    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plastic_pollution, container, false);
        textView = view.findViewById(R.id.textViewPlasticPollution);
        Button buttonQuizAndLearn = view.findViewById(R.id.buttonQuizAndLearn);

        // 퀴즈 버튼에 클릭 리스너 추가
        buttonQuizAndLearn.setOnClickListener(v -> loadQuizFragment());

        // 플라스틱 오염 개요 설명 텍스트 설정
        setPlasticPollutionOverview();

        // 뒤로 가기 버튼 설정
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // HomeFragment로 돌아가기
                FragmentManager fragmentManager = getParentFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack(); // 이전 프래그먼트로 이동
                } else {
                    requireActivity().finish(); // 백스택이 비어있으면 앱 종료
                }
            }
        });

        return view;
    }

    private void loadQuizFragment() {
        Fragment quizFragment = new PlasticQuizListFragment();
        FragmentManager fragmentManager = getParentFragmentManager(); // getParentFragmentManager() 사용
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, quizFragment); // fragment_container는 실제 프래그먼트 컨테이너 ID로 변경하세요.
        transaction.addToBackStack(null); // 백스택에 추가하여 뒤로가기 기능 추가
        transaction.commit();
    }

    private void setPlasticPollutionOverview() {
        String overview = "플라스틱 오염은 전 세계적으로 심각한 환경 문제 중 하나로, 수백만 톤의 플라스틱이 매년 바다로 유입되고 있습니다. "
                + "플라스틱은 분해되기까지 수백 년이 걸리며, 그 과정에서 미세 플라스틱으로 분해되어 해양 생물과 인간에게 해를 끼칠 수 있습니다. "
                + "플라스틱 제품의 남용과 폐기물 관리 부족은 환경을 오염시키고, 해양 생태계에 심각한 위협을 가하고 있습니다. "
                + "우리는 플라스틱 사용을 줄이고, 재활용을 늘리며, 플라스틱 대체재를 사용하는 등 플라스틱 오염을 줄이기 위한 노력을 기울여야 합니다.";

        textView.setText(overview);
    }
}
