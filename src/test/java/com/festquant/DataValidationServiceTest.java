package com.festquant;

import com.festquant.domain.Bid;
import com.festquant.exception.InvalidBidException;
import com.festquant.service.DataValidationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataValidationServiceTest {
    private final DataValidationService validator = new DataValidationService();

    @Test
    void validBidPassesValidation() {
        Bid bid = new Bid("B001", "E001", "U101", 2500, LocalDateTime.now());
        assertDoesNotThrow(() -> validator.validateBid(bid));
    }

    @Test
    void negativeBidFailsValidation() {
        Bid bid = new Bid("B001", "E001", "U101", -10, LocalDateTime.now());
        assertThrows(InvalidBidException.class, () -> validator.validateBid(bid));
    }

    @Test
    void invalidUserIdFailsValidation() {
        Bid bid = new Bid("B001", "E001", "USER101", 2500, LocalDateTime.now());
        assertThrows(InvalidBidException.class, () -> validator.validateBid(bid));
    }
}
