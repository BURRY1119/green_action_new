package com.example.green_action;

public class User {
    public String userId;         // Firebase 고유 ID
    public String email;          // 이메일 주소
    public String profileImage;   // 프로필 이미지 URL
    public String name;           // 이름
    public String contact;        // 연락처
    public String gender;         // 성별
    public String id;             // 사용자 아이디
    public String password;       // 사용자 비밀번호
    public int score;             // 사용자 점수
    public int rank;              // 사용자 랭크

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {}

    public User(String userId, String email, String profileImage, String name, String contact, String gender, String id, String password, int score, int rank) {
        this.userId = userId;
        this.email = email;
        this.profileImage = profileImage;
        this.name = name;
        this.contact = contact;
        this.gender = gender;
        this.id = id;
        this.password = password;
        this.score = score;
        this.rank = rank;
    }
}