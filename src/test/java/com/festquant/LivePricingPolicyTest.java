/**
 * Contains the live pricing policy test implementation used by FestQuant.
 */
package com.festquant;

import com.festquant.domain.Event;
import com.festquant.stream.LivePricingPolicy;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the expected behaviour of live pricing policy.
 */
class LivePricingPolicyTest {
    private final Event event = new Event(
            "E001", "Pro Night", "Music", "V001", 1000,
            800, 600, 1400, LocalDateTime.of(2026, 8, 10, 20, 0), 5, 1.4
    );
    private final LivePricingPolicy policy = new LivePricingPolicy();

    /**
     * Handles the higher live demand produces higher price step.
     */
    @Test
    void higherLiveDemandProducesHigherPrice() {
        // Holds the cooling price for this calculation.
        double coolingPrice = policy.recommend(event, 920, 0.40);
        // Holds the surge price for this calculation.
        double surgePrice = policy.recommend(event, 920, 1.80);

        assertTrue(surgePrice > coolingPrice);
    }

    /**
     * Handles the live price respects fairness bounds step.
     */
    @Test
    void livePriceRespectsFairnessBounds() {
        // Holds the low for this calculation.
        double low = policy.recommend(event, 300, 0.0);
        // Holds the high for this calculation.
        double high = policy.recommend(event, 2000, 5.0);

        assertTrue(low >= event.getBasePrice() * 0.85);
        assertTrue(high <= event.getBasePrice() * 1.20);
    }
}
