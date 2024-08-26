package com.example.green_action;

public class DailyQuiz {
    private int quizId;
    private QuizDetail quizDetail;
    private long timestamp;

    // Firebase에서 객체를 생성할 때 빈 생성자가 필요
    public DailyQuiz() {
    }

    public DailyQuiz(int quizId, QuizDetail quizDetail, long timestamp) {
        this.quizId = quizId;
        this.quizDetail = quizDetail;
        this.timestamp = timestamp;
    }

    // Getter 및 Setter 메서드
    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public QuizDetail getQuizDetail() {
        return quizDetail;
    }

    public void setQuizDetail(QuizDetail quizDetail) {
        this.quizDetail = quizDetail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}