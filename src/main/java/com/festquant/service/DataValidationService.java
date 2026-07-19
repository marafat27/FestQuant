/**
 * Contains the data validation service implementation used by FestQuant.
 */
package com.festquant.service;

import com.festquant.domain.Bid;
import com.festquant.exception.InvalidBidException;
import com.festquant.exception.InvalidEventException;

import java.util.regex.Pattern;

/**
 * Coordinates the business logic for data validation.
 */
public class DataValidationService {
    private static final Pattern BID_ID = Pattern.compile("B\\d{3}");
    private static final Pattern EVENT_ID = Pattern.compile("E\\d{3}");
    private static final Pattern USER_ID = Pattern.compile("U\\d{3}");
    // Stores the max realistic bid used by this class.
    private static final double MAX_REALISTIC_BID = 8000.0;

    /**
     * Validates event id.
     */
    public void validateEventId(String eventId) {
        if (eventId == null || !EVENT_ID.matcher(eventId).matches()) {
            throw new InvalidEventException("Invalid event id: " + eventId);
        }
    }

    /**
     * Validates user id.
     */
    public void validateUserId(String userId) {
        if (userId == null || !USER_ID.matcher(userId).matches()) {
            throw new InvalidBidException("Invalid user id: " + userId);
        }
    }

    /**
     * Validates bid.
     */
    public void validateBid(Bid bid) {
        if (bid == null) {
            throw new InvalidBidException("Bid cannot be null");
        }
        if (!BID_ID.matcher(bid.getBidId()).matches()) {
            throw new InvalidBidException("Invalid bid id: " + bid.getBidId());
        }
        validateEventId(bid.getEventId());
        validateUserId(bid.getUserId());
        if (bid.getBidAmount() <= 0) {
            throw new InvalidBidException("Bid amount must be positive");
        }
        if (bid.getBidAmount() > MAX_REALISTIC_BID) {
            throw new InvalidBidException("Bid amount is outside the realistic project range");
        }
    }
}
