package com.example.green_action;

public class Ranking {
    public String userId;
    public int rank;
    public long timestamp;

    public Ranking() {
        // Default constructor required for calls to DataSnapshot.getValue(Ranking.class)
    }

    public Ranking(String userId, int rank, long timestamp) {
        this.userId = userId;
        this.rank = rank;
        this.timestamp = timestamp;
    }
}