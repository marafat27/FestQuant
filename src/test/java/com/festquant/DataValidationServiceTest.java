/**
 * Contains the data validation service test implementation used by FestQuant.
 */
package com.festquant;

import com.festquant.domain.Bid;
import com.festquant.exception.InvalidBidException;
import com.festquant.service.DataValidationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks the expected behaviour of data validation service.
 */
class DataValidationServiceTest {
    private final DataValidationService validator = new DataValidationService();

    /**
     * Handles the valid bid passes validation step.
     */
    @Test
    void validBidPassesValidation() {
        // Holds the bid for this calculation.
        Bid bid = new Bid("B001", "E001", "U101", 2500, LocalDateTime.now());
        assertDoesNotThrow(() -> validator.validateBid(bid));
    }

    /**
     * Handles the negative bid fails validation step.
     */
    @Test
    void negativeBidFailsValidation() {
        // Holds the bid for this calculation.
        Bid bid = new Bid("B001", "E001", "U101", -10, LocalDateTime.now());
        assertThrows(InvalidBidException.class, () -> validator.validateBid(bid));
    }

    /**
     * Handles the invalid user id fails validation step.
     */
    @Test
    void invalidUserIdFailsValidation() {
        // Holds the bid for this calculation.
        Bid bid = new Bid("B001", "E001", "USER101", 2500, LocalDateTime.now());
        assertThrows(InvalidBidException.class, () -> validator.validateBid(bid));
    }
}
