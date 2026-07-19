/**
 * Contains the admin implementation used by FestQuant.
 */
package com.festquant.domain;

/**
 * Represents the admin part of the FestQuant application.
 */
public class Admin extends User {
    /**
     * Creates a Admin with the values needed by this component.
     */
    public Admin(String userId, String name, double walletBalance) {
        super(userId, name, UserType.ADMIN, walletBalance);
    }
}
