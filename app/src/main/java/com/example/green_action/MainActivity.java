package com.example.green_action;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar 및 DrawerLayout 초기화
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // DrawerLayout과 Toolbar 연결
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView 및 BottomNavigationView의 항목 선택 리스너 설정
        setupNavigationListeners(navigationView, bottomNavigationView);

        // 초기 화면 설정
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());  // 초기 프래그먼트 설정
        }

        // DrawerListener를 추가하여 드로어가 열릴 때 사용자 프로필 로드
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // 슬라이드 중일 때의 행동 (필요 시 구현)
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // 드로어가 열렸을 때 로그 출력
                Log.d("MainActivity", "Drawer opened");
                loadUserProfile();  // 드로어가 열렸을 때 사용자 프로필 데이터를 로드
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // 드로어가 닫혔을 때의 행동 (필요 시 구현)
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // 드로어 상태가 변경되었을 때의 행동 (필요 시 구현)
                Log.d("MainActivity", "Drawer state changed: " + newState);
            }
        });
    }

    // Firebase에서 사용자 데이터를 불러와 NavigationView 헤더에 설정
    private void loadUserProfile() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ImageView profileImageView = headerView.findViewById(R.id.imageView);
        TextView profileIdTextView = headerView.findViewById(R.id.textView);
        TextView profileInfoTextView = headerView.findViewById(R.id.textView2);

        // Firebase에서 사용자 ID를 가져오기 (예: FirebaseAuth에서)
        String userId = getUserId(); // 실제로 사용자 ID를 가져오는 로직 구현

        if (userId == null) {
            Toast.makeText(MainActivity.this, "사용자 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase에서 사용자 데이터 참조
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // 사용자 데이터 읽기 및 UI 업데이트
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("MainActivity", "DataSnapshot: " + dataSnapshot.toString());
                    String id = dataSnapshot.child("id").getValue(String.class);
                    Long score = dataSnapshot.child("score").getValue(Long.class);
                    String profileImage = dataSnapshot.child("profileImage").getValue(String.class);

                    if (id == null || score == null || profileImage == null) {
                        Log.e("MainActivity", "Missing fields in dataSnapshot");
                        Toast.makeText(MainActivity.this, "사용자 정보가 누락되었습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 사용자 정보 UI에 설정
                    profileIdTextView.setText(id);
                    profileInfoTextView.setText("점수: " + score);

                    // 프로필 이미지 로드
                    Glide.with(MainActivity.this)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(profileImageView);

                } else {
                    Log.e("MainActivity", "DataSnapshot does not exist");
                    Toast.makeText(MainActivity.this, "사용자 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "데이터베이스 오류: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserId() {
        // FirebaseAuth에서 사용자 ID를 가져오는 로직
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // NavigationView 및 BottomNavigationView의 항목 선택 리스너 설정
    private void setupNavigationListeners(NavigationView navigationView, BottomNavigationView bottomNavigationView) {
        // NavigationView 항목 클릭 리스너 설정
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                handleNavigationItemSelected(item.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);  // 드로어 닫기
                return true;
            }
        });

        // BottomNavigationView 아이템 클릭 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return handleBottomNavigationItemSelected(item.getItemId());
            }
        });
    }

    // NavigationView의 항목 선택 처리
    private void handleNavigationItemSelected(int itemId) {
        Fragment fragment = null;
        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_profile) {
            fragment = new SettingsFragment();
        }
        if (fragment != null) {
            loadFragment(fragment);
        }
    }

    // BottomNavigationView의 아이템 선택 처리
    private boolean handleBottomNavigationItemSelected(int itemId) {
        Fragment fragment = null;

        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_quiz) {
            fragment = createDailyQuizFragmentWithId(1); // 여기서 quizId를 전달
        } else if (itemId == R.id.nav_leaderboard) {
            fragment = new LeaderboardFragment();
        }

        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }
        return false;
    }

    // DailyQuizFragment에 quizId를 전달하는 메서드 추가
    private DailyQuizFragment createDailyQuizFragmentWithId(int quizId) {
        DailyQuizFragment fragment = new DailyQuizFragment();
        Bundle args = new Bundle();
        args.putInt("QUIZ_NUMBER", quizId);
        fragment.setArguments(args);
        return fragment;
    }

    // 프래그먼트를 로드하는 메서드
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}