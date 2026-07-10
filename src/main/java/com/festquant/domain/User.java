package com.festquant.domain;

public abstract class User {
    private final String userId;
    private final String name;
    private final UserType userType;
    private final double walletBalance;

    protected User(String userId, String name, UserType userType, double walletBalance) {
        this.userId = userId;
        this.name = name;
        this.userType = userType;
        this.walletBalance = walletBalance;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public UserType getUserType() { return userType; }
    public double getWalletBalance() { return walletBalance; }
}
