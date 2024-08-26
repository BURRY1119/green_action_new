package com.example.green_action;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

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
            fragment = new DailyQuizFragment();
        } else if (itemId == R.id.nav_leaderboard) {
            fragment = new LeaderboardFragment();
        }
        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }
        return false;
    }

    // 프래그먼트를 로드하는 메서드
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}