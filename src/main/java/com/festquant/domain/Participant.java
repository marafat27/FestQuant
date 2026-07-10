package com.festquant.domain;

public class Participant extends User {
    public Participant(String userId, String name, double walletBalance) {
        super(userId, name, UserType.PARTICIPANT, walletBalance);
    }
}
