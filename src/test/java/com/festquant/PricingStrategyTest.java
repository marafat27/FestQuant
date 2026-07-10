package com.festquant;

import com.festquant.domain.PriceRecommendation;
import com.festquant.domain.PricingEvent;
import com.festquant.domain.TicketSalePoint;
import com.festquant.model.ForecastResult;
import com.festquant.model.JavaNonlinearModelAdapter;
import com.festquant.pricing.PriceSimulationRow;
import com.festquant.pricing.RevenueMaximizingPricingStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PricingStrategyTest {
    @Test
    void recommendationRespectsBoundsAndFairnessCap() {
        PricingEvent event = new PricingEvent("E001", "Pro Night", "2026-08-10 20:00",
                800, 600, 1400, 1000, 5, 1.4);
        List<TicketSalePoint> history = List.of(
                new TicketSalePoint("E001", LocalDateTime.of(2026, 8, 1, 10, 0),
                        800, 900, 250, 30, 650)
        );
        List<PriceSimulationRow> simulations = new ArrayList<>();
        ForecastResult forecast = new ForecastResult("E001", 180, "RISING", .85, "Test forecast");

        PriceRecommendation result = new RevenueMaximizingPricingStrategy(new JavaNonlinearModelAdapter())
                .recommendPrice(event, history, forecast, simulations);

        assertTrue(result.getRecommendedPrice() >= 680);
        assertTrue(result.getRecommendedPrice() <= 960);
        assertTrue(result.getExpectedRevenue() >= 0);
        assertFalse(simulations.isEmpty());
    }
}
