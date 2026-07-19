/**
 * Contains the user implementation used by FestQuant.
 */
package com.festquant.domain;

/**
 * Represents the user part of the FestQuant application.
 */
public abstract class User {
    // Stores the user id used by this class.
    private final String userId;
    // Stores the name used by this class.
    private final String name;
    // Stores the user type used by this class.
    private final UserType userType;
    // Stores the wallet balance used by this class.
    private final double walletBalance;

    /**
     * Creates a User with the values needed by this component.
     */
    protected User(String userId, String name, UserType userType, double walletBalance) {
        this.userId = userId;
        this.name = name;
        this.userType = userType;
        this.walletBalance = walletBalance;
    }

    /**
     * Returns user id.
     */
    public String getUserId() { return userId; }
    /**
     * Returns name.
     */
    public String getName() { return name; }
    /**
     * Returns user type.
     */
    public UserType getUserType() { return userType; }
    /**
     * Returns wallet balance.
     */
    public double getWalletBalance() { return walletBalance; }
}
