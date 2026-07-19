/**
 * Contains the pricing strategy implementation used by FestQuant.
 */
package com.festquant.pricing;

import com.festquant.domain.PricingEvent;
import com.festquant.domain.PriceRecommendation;
import com.festquant.domain.TicketSalePoint;
import com.festquant.model.ForecastResult;

import java.util.List;

/**
 * Defines the operations required for pricing strategy.
 */
public interface PricingStrategy {
    /**
     * Recommends price.
     */
    PriceRecommendation recommendPrice(
            PricingEvent event,
            List<TicketSalePoint> history,
            ForecastResult forecast,
            List<PriceSimulationRow> simulationRows
    );
}
