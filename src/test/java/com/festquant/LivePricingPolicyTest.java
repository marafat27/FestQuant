package com.festquant;

import com.festquant.domain.Event;
import com.festquant.stream.LivePricingPolicy;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LivePricingPolicyTest {
    private final Event event = new Event(
            "E001", "Pro Night", "Music", "V001", 1000,
            800, 600, 1400, LocalDateTime.of(2026, 8, 10, 20, 0), 5, 1.4
    );
    private final LivePricingPolicy policy = new LivePricingPolicy();

    @Test
    void higherLiveDemandProducesHigherPrice() {
        double coolingPrice = policy.recommend(event, 920, 0.40);
        double surgePrice = policy.recommend(event, 920, 1.80);

        assertTrue(surgePrice > coolingPrice);
    }

    @Test
    void livePriceRespectsFairnessBounds() {
        double low = policy.recommend(event, 300, 0.0);
        double high = policy.recommend(event, 2000, 5.0);

        assertTrue(low >= event.getBasePrice() * 0.85);
        assertTrue(high <= event.getBasePrice() * 1.20);
    }
}
