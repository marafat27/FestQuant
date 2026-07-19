/**
 * Contains the participant implementation used by FestQuant.
 */
package com.festquant.domain;

/**
 * Represents the participant part of the FestQuant application.
 */
public class Participant extends User {
    /**
     * Creates a Participant with the values needed by this component.
     */
    public Participant(String userId, String name, double walletBalance) {
        super(userId, name, UserType.PARTICIPANT, walletBalance);
    }
}
