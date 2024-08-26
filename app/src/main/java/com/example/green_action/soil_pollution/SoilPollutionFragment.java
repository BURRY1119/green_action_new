package com.example.green_action.soil_pollution;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.green_action.R;

public class SoilPollutionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Layout을 인플레이트하고 View를 반환합니다
        return inflater.inflate(R.layout.fragment_soil_pollution, container, false);
    }
}