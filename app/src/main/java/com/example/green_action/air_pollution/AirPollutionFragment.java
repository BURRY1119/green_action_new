package com.example.green_action.air_pollution;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import android.os.AsyncTask;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.green_action.R;
import com.example.green_action.air_pollution.AirQuizListFragment;

public class AirPollutionFragment extends Fragment {

    private static final String API_KEY = "zgDi2jCAAHkGbiYY9vTynvRLYSU3sGls9eAJM4HnHCgjj5AQM05gxkuESMijNOcgGJS+FBii9jYfBtH+Zs4ESQ==";
    private static final String BASE_URL = "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty";
    private LinearLayout linearLayoutContainer;
    private TextView airQualityIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air_pollution, container, false);
        linearLayoutContainer = view.findViewById(R.id.linearLayoutContainer);
        airQualityIndicator = view.findViewById(R.id.airQualityIndicator);
        Button buttonQuizAndLearn = view.findViewById(R.id.buttonQuizAndLearn);

        // 지역 선택 버튼
        Button btnSeoul = view.findViewById(R.id.btnSeoul);
        Button btnGyeonggi = view.findViewById(R.id.btnGyeonggi);
        Button btnIncheon = view.findViewById(R.id.btnIncheon);
        Button btnGangwon = view.findViewById(R.id.btnGangwon);
        Button btnChungcheong = view.findViewById(R.id.btnChungcheong);
        Button btnJeolla = view.findViewById(R.id.btnJeolla);
        Button btnGyeongsang = view.findViewById(R.id.btnGyeongsang);
        Button btnJeju = view.findViewById(R.id.btnJeju);

        // 각 버튼에 대한 클릭 리스너 설정
        btnSeoul.setOnClickListener(v -> {
            resetButtonColors();
            btnSeoul.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.actionGreen)); // 버튼 클릭 시 actionGreen 색상으로 변경
            fetchAirPollutionData("서울");
        });

        btnGyeonggi.setOnClickListener(v -> {
            resetButtonColors();
            btnGyeonggi.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.actionGreen));
            fetchAirPollutionData("경기");
        });

        btnIncheon.setOnClickListener(v -> {
            resetButtonColors();
            btnIncheon.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.actionGreen));
            fetchAirPollutionData("인천");
        });

        btnGangwon.setOnClickListener(v -> {
            resetButtonColors();
            btnGangwon.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.actionGreen));
            fetchAirPollutionData("강원");
        });

        btnChungcheong.setOnClickListener(v -> {
            resetButtonColors();
            btnChungcheong.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.actionGreen));
            fetchAirPollutionData("대전");
        });

        btnJeolla.setOnClickListener(v -> {
            resetButtonColors();
            btnJeolla.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.actionGreen));
            fetchAirPollutionData("광주");
        });

        btnGyeongsang.setOnClickListener(v -> {
            resetButtonColors();
            btnGyeongsang.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.actionGreen));
            fetchAirPollutionData("부산");
        });

        btnJeju.setOnClickListener(v -> {
            resetButtonColors();
            btnJeju.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.actionGreen));
            fetchAirPollutionData("제주");
        });

        // 퀴즈 버튼에 클릭 리스너 추가
        buttonQuizAndLearn.setOnClickListener(v -> loadQuizFragment());

        // 초기 데이터 가져오기 (서울 지역으로 설정)
        fetchAirPollutionData("서울");

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
        Fragment quizFragment = new AirQuizListFragment(); // AirQuizList 프래그먼트 생성
        FragmentManager fragmentManager = getParentFragmentManager(); // getParentFragmentManager() 사용
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, quizFragment); // fragment_container는 실제 프래그먼트 컨테이너 ID로 변경하세요.
        transaction.addToBackStack(null); // 백스택에 추가하여 뒤로가기 기능 추가
        transaction.commit();
    }

    private void fetchAirPollutionData(String region) {
        new FetchAirPollutionTask().execute(region); // 선택한 지역 데이터를 가져옵니다.
    }

    private void resetButtonColors() {
        View view = getView();
        if (view != null) {
            int defaultColor = getResources().getColor(android.R.color.holo_green_light);
            view.findViewById(R.id.btnSeoul).setBackgroundColor(defaultColor);
            view.findViewById(R.id.btnGyeonggi).setBackgroundColor(defaultColor);
            view.findViewById(R.id.btnIncheon).setBackgroundColor(defaultColor);
            view.findViewById(R.id.btnGangwon).setBackgroundColor(defaultColor);
            view.findViewById(R.id.btnChungcheong).setBackgroundColor(defaultColor);
            view.findViewById(R.id.btnJeolla).setBackgroundColor(defaultColor);
            view.findViewById(R.id.btnGyeongsang).setBackgroundColor(defaultColor);
            view.findViewById(R.id.btnJeju).setBackgroundColor(defaultColor);
        }
    }

    private class FetchAirPollutionTask extends AsyncTask<String, Void, List<Map<String, String>>> {

        @Override
        protected List<Map<String, String>> doInBackground(String... params) {
            String sidoName = params[0];
            OkHttpClient client = new OkHttpClient();

            try {
                String url = BASE_URL + "?serviceKey=" + URLEncoder.encode(API_KEY, "UTF-8")
                        + "&returnType=xml&numOfRows=100&pageNo=1&sidoName=" + URLEncoder.encode(sidoName, "UTF-8") + "&ver=1.3"; // numOfRows=100으로 변경
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String xmlData = response.body().string();
                    return parseXml(xmlData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> result) {
            if (result != null && !result.isEmpty()) {
                linearLayoutContainer.removeAllViews(); // 기존 데이터 지우기
                for (Map<String, String> item : result) {
                    String stationName = item.get("stationName");
                    String khaiValue = item.get("khaiValue");
                    String so2Value = item.get("so2Value");
                    String coValue = item.get("coValue");
                    String o3Value = item.get("o3Value");
                    String no2Value = item.get("no2Value");
                    String pm10Value = item.get("pm10Value");
                    int khaiValueInt = 0;
                    try {
                        khaiValueInt = Integer.parseInt(khaiValue.equals("N/A") ? "0" : khaiValue);
                    } catch (NumberFormatException e) {
                        khaiValueInt = -1; // 잘못된 값일 경우 -1로 설정
                    }

                    // KHAI 값에 따른 색상 결정
                    int color = getColorForKhaiValue(khaiValueInt);

                    // 새로운 TextView 생성
                    TextView stationTextView = new TextView(getContext());
                    stationTextView.setText("지역: " + stationName +
                            "\n통합대기환경지수(KHAI): " + khaiValue +
                            "\n이산화황(SO2): " + so2Value +
                            "\n일산화탄소(CO): " + coValue +
                            "\n오존(O3): " + o3Value +
                            "\n이산화질소(NO2): " + no2Value +
                            "\n미세먼지(PM10): " + pm10Value);
                    stationTextView.setBackgroundColor(color);
                    stationTextView.setTextColor(Color.BLACK); // 텍스트 색상 설정
                    stationTextView.setTypeface(null, Typeface.BOLD); // 텍스트를 Bold로 설정
                    stationTextView.setPadding(10, 10, 10, 10);

                    // 추가
                    linearLayoutContainer.addView(stationTextView);
                    // 수평선 추가
                    View divider = new View(getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 10); // 높이를 5px로 설정
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor(Color.GRAY); // 수평선 색상 설정
                    linearLayoutContainer.addView(divider);
                }
            } else {
                TextView errorTextView = new TextView(getContext());
                errorTextView.setText("데이터를 가져오는 데 실패했습니다.");
                linearLayoutContainer.addView(errorTextView);
            }
        }

        private int getColorForKhaiValue(int khaiValue) {
            if (khaiValue >= 0 && khaiValue <= 50) {
                return Color.parseColor("#ADD8E6"); // 밝은 파랑 (좋음)
            } else if (khaiValue >= 51 && khaiValue <= 100) {
                return ContextCompat.getColor(getContext(), R.color.airPollution); // 보통일 때 @color/airPollution 사용
            } else if (khaiValue >= 101 && khaiValue <= 250) {
                return Color.parseColor("#FFFFE0"); // 밝은 노랑 (나쁨)
            } else if (khaiValue >= 251) {
                return Color.parseColor("#FFCCCB"); // 밝은 빨강 (매우 나쁨)
            } else {
                return Color.WHITE; // 정보 없음 또는 잘못된 값 (흰색으로 설정)
            }
        }

        private List<Map<String, String>> parseXml(String xmlData) throws Exception {
            List<Map<String, String>> dataList = new ArrayList<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("item");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                Map<String, String> dataMap = new HashMap<>();
                String[] tags = {"stationName", "mangName", "sidoName", "dataTime", "so2Value", "coValue", "o3Value", "no2Value",
                        "pm10Value", "pm10Value24", "pm25Value", "pm25Value24", "khaiValue", "khaiGrade", "so2Grade",
                        "coGrade", "o3Grade", "no2Grade", "pm10Grade", "pm25Grade", "pm10Grade1h", "pm25Grade1h",
                        "so2Flag", "coFlag", "o3Flag", "no2Flag", "pm10Flag", "pm25Flag"};

                for (String tag : tags) {
                    dataMap.put(tag, getTagValue(tag, element));
                }
                dataList.add(dataMap);
            }
            return dataList;
        }

        private String getTagValue(String tag, Element element) {
            NodeList nodeList = element.getElementsByTagName(tag);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
            return "N/A"; // 데이터가 없을 경우 기본값
        }
    }
}
