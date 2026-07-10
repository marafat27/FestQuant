package com.festquant.domain;

public class Admin extends User {
    public Admin(String userId, String name, double walletBalance) {
        super(userId, name, UserType.ADMIN, walletBalance);
    }
}
