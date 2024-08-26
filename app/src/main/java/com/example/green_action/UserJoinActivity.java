package com.example.green_action;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.green_action.remote.FirebaseClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.regex.Pattern;

public class UserJoinActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth mAuth;
    private static final String TAG = "UserJoin";
    private Uri profileImageUri;
    private StorageReference storageRef;
    private FirebaseClient firebaseClient;
    private boolean isUsernameAvailable = false;

    private EditText userId;
    private EditText userEmail;
    private EditText userPassword;
    private EditText userPwCk;
    private EditText userName;
    private EditText userContact;
    private RadioGroup userGenderGroup;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_join);

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        firebaseClient = new FirebaseClient();

        // Initialize UI components
        userEmail = findViewById(R.id.join_email);
        userId = findViewById(R.id.join_id);
        userPassword = findViewById(R.id.join_password);
        userPwCk = findViewById(R.id.join_pwck);
        userName = findViewById(R.id.join_name);
        userContact = findViewById(R.id.join_phone);
        userGenderGroup = findViewById(R.id.join_sex_group);
        profileImageView = findViewById(R.id.profile_image_view);

        Button selectImageButton = findViewById(R.id.select_profile_image);
        Button checkDuplicateButton = findViewById(R.id.check_duplicate);
        Button confirm = findViewById(R.id.complete);

        selectImageButton.setOnClickListener(v -> openFileChooser());
        checkDuplicateButton.setOnClickListener(v -> checkDuplicateId());

        confirm.setOnClickListener(v -> {
            if (validateInput()) {
                String email = userEmail.getText().toString().trim();
                String id = userId.getText().toString().trim();
                String password = userPassword.getText().toString().trim();
                String passwordCk = userPwCk.getText().toString().trim();
                String name = userName.getText().toString().trim();
                String contact = userContact.getText().toString().trim();

                int selectedGenderId = userGenderGroup.getCheckedRadioButtonId();
                RadioButton selectedGenderButton = findViewById(selectedGenderId);
                String gender = selectedGenderButton.getText().toString().trim();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(UserJoinActivity.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    if (profileImageUri != null) {
                                        StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(profileImageUri));
                                        fileReference.putFile(profileImageUri).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                            String imageUrl = uri.toString();
                                            saveUserData(user.getUid(), email, id, password, name, contact, gender, imageUrl);
                                        })).addOnFailureListener(e -> Toast.makeText(UserJoinActivity.this, "프로필 이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    } else {
                                        saveUserData(user.getUid(), email, id, password, name, contact, gender, "");
                                    }
                                }
                            } else {
                                String errorMessage = "회원가입 실패: " + Objects.requireNonNull(task.getException()).getMessage();
                                Log.w(TAG, errorMessage);
                                Toast.makeText(UserJoinActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileImageView.setImageURI(profileImageUri);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void saveUserData(String userId, String email, String id, String password, String name, String contact, String gender, String imageUrl) {
        User newUser = new User(
                userId,
                email,
                imageUrl,
                name,
                contact,
                gender,
                id,
                password,
                0,
                0
        );

        firebaseClient.saveUserData(userId, newUser);

        Toast.makeText(UserJoinActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(UserJoinActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkDuplicateId() {
        String id = userId.getText().toString().trim();

        // 아이디 입력 확인
        if (id.isEmpty()) {
            Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        // 아이디 형식 검사
        if (!isValidId(id)) {
            Toast.makeText(this, "아이디는 6~12자의 영문, 숫자, -, _만 사용 가능합니다.", Toast.LENGTH_LONG).show();
            return;
        }

        // 아이디 중복 확인
        firebaseClient.isIDExists(id, isAvailable -> {
            if (isAvailable) {
                Toast.makeText(UserJoinActivity.this, "아이디가 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                isUsernameAvailable = false; // 중복된 경우 false로 설정
            } else {
                Toast.makeText(UserJoinActivity.this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                isUsernameAvailable = true; // 사용 가능한 경우 true로 설정
            }
        });
    }

    private boolean validateInput() {
        String email = userEmail.getText().toString().trim();
        String id = userId.getText().toString().trim();
        String password = userPassword.getText().toString().trim();
        String passwordCk = userPwCk.getText().toString().trim();
        String name = userName.getText().toString().trim();
        String contact = userContact.getText().toString().trim();

        int selectedGenderId = userGenderGroup.getCheckedRadioButtonId();

        if (email.isEmpty() || id.isEmpty() || password.isEmpty() || passwordCk.isEmpty() ||
                name.isEmpty() || contact.isEmpty() || selectedGenderId == -1) {
            Toast.makeText(UserJoinActivity.this, "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isUsernameAvailable) {
            Toast.makeText(UserJoinActivity.this, "아이디 중복 체크를 해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidId(id)) {
            Toast.makeText(UserJoinActivity.this, "아이디는 6~12자의 영문, 숫자, -, _만 사용 가능합니다.", Toast.LENGTH_SHORT).show();
            userId.requestFocus();
            return false;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(UserJoinActivity.this, "비밀번호는 8~20자의 영문, 숫자, 특수문자 중 2가지 이상만 사용 가능합니다.", Toast.LENGTH_SHORT).show();
            userPassword.requestFocus();
            return false;
        }

        if (!password.equals(passwordCk)) {
            Toast.makeText(UserJoinActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            userPwCk.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(UserJoinActivity.this, "이메일 형식이 올바르지 않습니다.(예: example@example.com)", Toast.LENGTH_SHORT).show();
            userEmail.requestFocus();
            return false;
        }

        if (!isValidName(name)) {
            Toast.makeText(UserJoinActivity.this, "이름 형식이 올바르지 않습니다.(예: 홍길동)", Toast.LENGTH_SHORT).show();
            userName.requestFocus();
            return false;
        }

        if (!isValidContact(contact)) {
            Toast.makeText(UserJoinActivity.this, "전화번호 형식이 올바르지 않습니다.(예: 01012345678)", Toast.LENGTH_SHORT).show();
            userContact.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidId(String id) {
        // 아이디는 영문자, 숫자, 기호(-, _) 사용 가능, 6~12자
        return Pattern.matches("^[a-zA-Z0-9-_]{6,12}$", id);
    }

    private boolean isValidPassword(String password) {
        // 비밀번호는 영문자, 숫자, 특수문자 중 2가지 이상 사용, 8~20자
        return Pattern.matches("^(?=.*[a-zA-Z])(?=.*\\d|.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$", password);
    }

    private boolean isValidEmail(String email) {
        // 이메일 형식 검증
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email);
    }

    private boolean isValidName(String name) {
        // 이름은 한국어로만 입력 가능, 18자 이하
        return Pattern.matches("^[가-힣]{1,18}$", name);
    }

    private boolean isValidContact(String contact) {
        // 전화번호는 11자리 숫자
        return Pattern.matches("^\\d{11}$", contact);
    }
}
