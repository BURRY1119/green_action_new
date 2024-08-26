package com.example.green_action;

import androidx.lifecycle.ViewModel;

public class QuizViewModel extends ViewModel {
    private int currentMaxScore;
    private int attemptsLeft;

    public int getCurrentMaxScore() {
        return currentMaxScore;
    }

    public void setCurrentMaxScore(int currentMaxScore) {
        this.currentMaxScore = currentMaxScore;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public void setAttemptsLeft(int attemptsLeft) {
        this.attemptsLeft = attemptsLeft;
    }
}