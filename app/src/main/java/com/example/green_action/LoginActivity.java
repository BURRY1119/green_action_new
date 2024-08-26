package com.example.green_action;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.green_action.remote.FirebaseClient; // FirebaseClient를 통해 사용자 정보 저장
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;  // Google Sign-In 요청 코드

    private FirebaseAuth mAuth;
    private EditText id, pw;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseClient firebaseClient; // FirebaseClient 인스턴스 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FirebaseAuth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        // FirebaseClient 인스턴스 초기화
        firebaseClient = new FirebaseClient();

        // 이미 로그인된 사용자가 있는지 확인하고, 있으면 메인 액티비티로 이동
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 로그인 화면 레이아웃 설정
        setContentView(R.layout.activity_login);

        // EditText와 Button 연결
        id = findViewById(R.id.login_id);
        pw = findViewById(R.id.login_password);
        Button loginButton = findViewById(R.id.login_button);
        Button joinButton = findViewById(R.id.join_button);
        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);

        // Google Sign-In 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Firebase Console에서 얻은 Web client ID
                .requestEmail()
                .build();

        // GoogleSignInClient 생성
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 회원가입 버튼 클릭 시 회원가입 액티비티로 이동
        joinButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, UserJoinActivity.class);
            startActivity(intent);
        });

        // 로그인 버튼 클릭 시 이메일과 비밀번호로 Firebase 인증
        loginButton.setOnClickListener(v -> {
            String email = id.getText().toString().trim();
            String password = pw.getText().toString().trim();

            // 이메일 입력 여부 확인
            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "이메일 주소를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 비밀번호 입력 여부 확인
            if (password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase를 통한 이메일/비밀번호 로그인 처리
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // 로그인 성공 시 메인 액티비티로 이동
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("user_uid", user.getUid());
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // 로그인 실패 시 이유 출력
                            String errorMessage;
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidUserException e) {
                                errorMessage = "존재하지 않는 계정입니다. 회원가입을 해주세요.";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                errorMessage = "이메일 또는 비밀번호가 올바르지 않습니다.";
                            } catch (Exception e) {
                                errorMessage = "로그인 실패: " + e.getMessage();
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Google Sign-In 버튼 클릭 시 Google 로그인 시도
        googleSignInButton.setOnClickListener(v -> signIn());
    }

    // Google Sign-In 요청 메서드
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // Google Sign-In 결과 처리
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    // 성공적으로 로그인된 경우 Firebase로 인증 진행
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    // 계정 정보를 가져오지 못한 경우
                    Toast.makeText(LoginActivity.this, "Google 로그인 실패: 계정 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                // Google Sign-In 실패 시 에러 메시지 출력
                String errorMessage = "Google 로그인 실패: ";
                switch (e.getStatusCode()) {
                    case GoogleSignInStatusCodes.NETWORK_ERROR:
                        errorMessage += "네트워크 오류가 발생했습니다. 인터넷 연결을 확인하세요.";
                        break;
                    case GoogleSignInStatusCodes.DEVELOPER_ERROR:
                        errorMessage += "개발자 오류가 발생했습니다. Google 로그인 설정을 확인하세요.";
                        break;
                    case GoogleSignInStatusCodes.INVALID_ACCOUNT:
                        errorMessage += "유효하지 않은 계정입니다.";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                        errorMessage += "로그인이 취소되었습니다.";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                        errorMessage += "로그인에 실패했습니다. 다시 시도하세요.";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_REQUIRED:
                        errorMessage += "로그인이 필요합니다.";
                        break;
                    default:
                        errorMessage += e.getMessage();
                        break;
                }
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Firebase Auth와 Google Sign-In 통합
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Google 로그인 성공 시 메인 액티비티로 이동
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Google 로그인 사용자의 정보를 Firebase Database에 저장
                            saveGoogleUserData(user);
                            Toast.makeText(LoginActivity.this, "Google 로그인 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("user_uid", user.getUid());
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Google 로그인 실패 시 이유 출력
                        String errorMessage;
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidUserException e) {
                            errorMessage = "존재하지 않는 계정입니다. 회원가입을 해주세요.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            errorMessage = "인증에 실패했습니다. 다시 시도해주세요.";
                        } catch (Exception e) {
                            errorMessage = "Google 로그인 실패: " + e.getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }


// Google 로그인 사용자 정보를 Firebase Database에 저장
    private void saveGoogleUserData(FirebaseUser user) {
        String userId = user.getUid();
        String userName = user.getDisplayName() != null ? user.getDisplayName() : "Unknown User";
        String userEmail = user.getEmail();
        String profileImage = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        // 필요한 필드만 사용하여 User 객체 생성
        User newUser = new User(userId, userEmail, profileImage, userName, "", "", userId, "", 0, 0);

        // FirebaseClient를 통해 사용자 정보 저장
        firebaseClient.saveUserData(userId, newUser); // userId를 key로 사용하여 데이터 저장
    }

}
