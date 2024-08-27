package com.example.green_action.soil_pollution;

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

public class SoilPollutionFragment extends Fragment {

    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_soil_pollution, container, false);
        textView = view.findViewById(R.id.textViewSoilPollution);
        Button buttonQuizAndLearn = view.findViewById(R.id.buttonQuizAndLearn);

        // 퀴즈 버튼에 클릭 리스너 추가
        buttonQuizAndLearn.setOnClickListener(v -> loadQuizFragment());

        // 토양 오염 개요 설명 텍스트 설정
        setSoilPollutionOverview();

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
        Fragment quizFragment = new SoilQuizListFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, quizFragment); // fragment_container는 실제 프래그먼트 컨테이너 ID로 변경하세요.
        transaction.addToBackStack(null); // 백스택에 추가하여 뒤로가기 기능 추가
        transaction.commit();
    }

    private void setSoilPollutionOverview() {
        String overview = "토양 오염은 주로 산업 폐기물, 농약, 화학 비료, 기름 유출 등으로 인해 발생합니다. "
                + "이러한 오염 물질은 토양에 축적되어 식물과 동물, 그리고 인간에게 유해한 영향을 미칠 수 있습니다. "
                + "토양 오염은 농작물의 질을 저하시켜 식량 안전에 위협이 될 수 있으며, 지하수 오염을 통해 널리 퍼질 수 있습니다. "
                + "따라서, 우리는 토양 오염을 방지하고, 오염된 토양을 정화하기 위한 노력을 기울여야 합니다. "
                + "환경을 보호하고 건강을 유지하기 위해, 오염원 관리와 친환경 농업 방법의 사용이 중요합니다.";

        textView.setText(overview);
    }
}
