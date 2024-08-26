package com.example.green_action.air_pollution;

import android.os.AsyncTask;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AirPollutionFragment extends Fragment {

    private static final String API_KEY = "zgDi2jCAAHkGbiYY9vTynvRLYSU3sGls9eAJM4HnHCgjj5AQM05gxkuESMijNOcgGJS+FBii9jYfBtH+Zs4ESQ==";
    private static final String BASE_URL = "https://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty";
    private TextView textView;
    private Button buttonQuizAndLearn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_air_pollution, container, false);
        textView = view.findViewById(R.id.textViewAirPollution);
        buttonQuizAndLearn = view.findViewById(R.id.buttonQuizAndLearn);

        // 퀴즈 버튼에 클릭 리스너 추가
        buttonQuizAndLearn.setOnClickListener(v -> loadQuizFragment());

        // 공기오염 데이터 가져오기
        fetchAirPollutionData();

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

    private void fetchAirPollutionData() {
        new FetchAirPollutionTask().execute("경기"); // 예시로 '경기' 데이터를 가져옵니다.
    }

    private class FetchAirPollutionTask extends AsyncTask<String, Void, List<Map<String, String>>> {

        @Override
        protected List<Map<String, String>> doInBackground(String... params) {
            String sidoName = params[0];
            OkHttpClient client = new OkHttpClient();

            try {
                String url = BASE_URL + "?serviceKey=" + URLEncoder.encode(API_KEY, "UTF-8")
                        + "&returnType=xml&numOfRows=10&pageNo=1&sidoName=" + URLEncoder.encode(sidoName, "UTF-8") + "&ver=1.3";
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
                StringBuilder stringBuilder = new StringBuilder();
                for (Map<String, String> item : result) {
                    for (Map.Entry<String, String> entry : item.entrySet()) {
                        stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    stringBuilder.append("\n");
                }
                textView.setText(stringBuilder.toString());
            } else {
                textView.setText("데이터를 가져오는 데 실패했습니다.");
            }
        }

        private List<Map<String, String>> parseXml(String xmlData) throws Exception {
            List<Map<String, String>> dataList = new ArrayList<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xmlData.getBytes("UTF-8")));
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