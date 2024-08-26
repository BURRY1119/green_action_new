package com.example.green_action;

public class QuizDetail {
    private String study;
    private String answer;
    private String question;
    private String quizId;
    private int answerLength;
    private int attemptsAllowed;
    private String explanation;
    private int maxScore;

    // 기본 생성자
    public QuizDetail() {
    }

    // 모든 필드를 포함한 생성자
    public QuizDetail(String study, String answer, String question, String quizId, int answerLength, int attemptsAllowed, String explanation, int maxScore) {
        this.study = study;
        this.answer = answer;
        this.question = question;
        this.quizId = quizId;
        this.answerLength = answerLength;
        this.attemptsAllowed = attemptsAllowed;
        this.explanation = explanation;
        this.maxScore = maxScore;
    }

    // Getter와 Setter 메서드
    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public int getAnswerLength() {
        return answerLength;
    }

    public void setAnswerLength(int answerLength) {
        this.answerLength = answerLength;
    }

    public int getAttemptsAllowed() {
        return attemptsAllowed;
    }

    public void setAttemptsAllowed(int attemptsAllowed) {
        this.attemptsAllowed = attemptsAllowed;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    // 정답 확인 메서드
    public boolean checkAnswer(String userAnswer) {
        return userAnswer != null && userAnswer.equalsIgnoreCase(this.answer.trim());
    }


}
