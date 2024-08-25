package com.example.green_action;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quiz_database";
    private static final int DATABASE_VERSION = 3; // 데이터베이스 버전 증가

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // user_table 테이블 생성 쿼리
        String createUserTableQuery = "CREATE TABLE user_table (" +
                "user_id TEXT PRIMARY KEY, " +
                "user_pw TEXT NOT NULL, " +
                "username TEXT NOT NULL, " +
                "email TEXT, " +
                "contact TEXT, " +
                "gender TEXT, " +
                "profileImage TEXT" +
                ")";
        db.execSQL(createUserTableQuery);

        // air_pollution_quiz 테이블 생성
        String createAirPollutionQuizTableQuery = "CREATE TABLE air_pollution_quiz (" +
                "quiz_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT NOT NULL, " +
                "study TEXT, " +
                "correct_answer TEXT NOT NULL, " +
                "correct_answer_length INTEGER, " +
                "explanation TEXT, " +
                "max_score INTEGER DEFAULT 3, " +
                "attempts_allowed INTEGER DEFAULT 3, " +
                "is_locked INTEGER DEFAULT 1, " +  // 1: 잠금, 0: 잠금 해제
                "is_solved INTEGER DEFAULT 0, " +  // 0: 미해결, 1: 해결됨
                "order_index INTEGER NOT NULL" +
                ")";
        db.execSQL(createAirPollutionQuizTableQuery);

        // soil_pollution_quiz 테이블 생성
        String createSoilPollutionQuizTableQuery = "CREATE TABLE soil_pollution_quiz (" +
                "quiz_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT NOT NULL, " +
                "study TEXT, " +
                "correct_answer TEXT NOT NULL, " +
                "correct_answer_length INTEGER, " +
                "explanation TEXT, " +
                "max_score INTEGER DEFAULT 3, " +
                "attempts_allowed INTEGER DEFAULT 3, " +
                "is_locked INTEGER DEFAULT 1, " +  // 1: 잠금, 0: 잠금 해제
                "is_solved INTEGER DEFAULT 0, " +  // 0: 미해결, 1: 해결됨
                "order_index INTEGER NOT NULL" +
                ")";
        db.execSQL(createSoilPollutionQuizTableQuery);

        // water_pollution_quiz 테이블 생성
        String createWaterPollutionQuizTableQuery = "CREATE TABLE water_pollution_quiz (" +
                "quiz_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT NOT NULL, " +
                "study TEXT, " +
                "correct_answer TEXT NOT NULL, " +
                "correct_answer_length INTEGER, " +
                "explanation TEXT, " +
                "max_score INTEGER DEFAULT 3, " +
                "attempts_allowed INTEGER DEFAULT 3, " +
                "is_locked INTEGER DEFAULT 1, " +  // 1: 잠금, 0: 잠금 해제
                "is_solved INTEGER DEFAULT 0, " +  // 0: 미해결, 1: 해결됨
                "order_index INTEGER NOT NULL" +
                ")";
        db.execSQL(createWaterPollutionQuizTableQuery);

        // plastic_pollution_quiz 테이블 생성
        String createPlasticPollutionQuizTableQuery = "CREATE TABLE plastic_pollution_quiz (" +
                "quiz_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "question TEXT NOT NULL, " +
                "study TEXT, " +
                "correct_answer TEXT NOT NULL, " +
                "correct_answer_length INTEGER, " +
                "explanation TEXT, " +
                "max_score INTEGER DEFAULT 3, " +
                "attempts_allowed INTEGER DEFAULT 3, " +
                "is_locked INTEGER DEFAULT 1, " +  // 1: 잠금, 0: 잠금 해제
                "is_solved INTEGER DEFAULT 0, " +  // 0: 미해결, 1: 해결됨
                "order_index INTEGER NOT NULL" +
                ")";
        db.execSQL(createPlasticPollutionQuizTableQuery);

        // quiz_progress 테이블 생성
        String createQuizProgressTableQuery = "CREATE TABLE quiz_progress (" +
                "user_id TEXT NOT NULL, " +
                "quiz_id INTEGER NOT NULL, " +
                "is_solved INTEGER DEFAULT 0, " +  // 0: 미해결, 1: 해결됨
                "PRIMARY KEY (user_id, quiz_id)," +
                "FOREIGN KEY (user_id) REFERENCES user_table(user_id) ON DELETE CASCADE" +
                ")";
        db.execSQL(createQuizProgressTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 테이블을 업데이트할 때 기존 테이블 삭제
        if (oldVersion < 3) {
            // 새로 추가된 `quiz_progress` 테이블만 삭제할 경우
            db.execSQL("DROP TABLE IF EXISTS quiz_progress");
            // 다른 테이블도 삭제할 경우 필요에 따라 추가
        }
        db.execSQL("DROP TABLE IF EXISTS user_table");
        db.execSQL("DROP TABLE IF EXISTS air_pollution_quiz");
        db.execSQL("DROP TABLE IF EXISTS soil_pollution_quiz");
        db.execSQL("DROP TABLE IF EXISTS water_pollution_quiz");
        db.execSQL("DROP TABLE IF EXISTS plastic_pollution_quiz");
        onCreate(db);
    }
}