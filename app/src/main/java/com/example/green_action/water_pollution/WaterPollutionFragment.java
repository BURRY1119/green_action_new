package com.example.green_action.water_pollution;

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

public class WaterPollutionFragment extends Fragment {

    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_water_pollution, container, false);
        textView = view.findViewById(R.id.textViewWaterPollution);
        Button buttonQuizAndLearn = view.findViewById(R.id.buttonQuizAndLearn);

        // 퀴즈 버튼에 클릭 리스너 추가
        buttonQuizAndLearn.setOnClickListener(v -> loadQuizFragment());

        // 수질 오염 개요 설명 텍스트 설정
        setWaterPollutionOverview();

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
        Fragment quizFragment = new WaterQuizListFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, quizFragment); // fragment_container는 실제 프래그먼트 컨테이너 ID로 변경하세요.
        transaction.addToBackStack(null); // 백스택에 추가하여 뒤로가기 기능 추가
        transaction.commit();
    }

    private void setWaterPollutionOverview() {
        String overview = "수질 오염은 인간과 생태계에 치명적인 영향을 미치는 중요한 환경 문제입니다. "
                + "수질 오염의 주요 원인은 산업 폐기물, 가정용 폐수, 농업용 비료 및 살충제 등입니다. \n\n"
                + "이러한 오염 물질은 하천과 바다로 흘러들어가 물고기와 해양 생물을 해치며, 인간의 식수에도 위협이 될 수 있습니다. "
                + "대기 오염과 마찬가지로, 수질 오염을 막는 것은 우리의 건강과 지구의 미래를 보호하기 위해 매우 중요합니다. "
                + "깨끗한 물을 유지하는 것은 모든 생명체의 생존을 위해 필수적입니다. "
                + "\n\n따라서, 우리는 수질 오염을 줄이기 위한 노력을 다해야 하며, 이를 위해 폐수를 적절히 처리하고 화학물질 사용을 줄이는 등의 방안을 강구해야 합니다.";

        textView.setText(overview);
    }
}
