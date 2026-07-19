/**
 * Contains the time series forecaster test implementation used by FestQuant.
 */
package com.festquant;

import com.festquant.domain.TicketSalePoint;
import com.festquant.model.ForecastResult;
import com.festquant.model.TimeSeriesForecaster;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the expected behaviour of time series forecaster.
 */
class TimeSeriesForecasterTest {
    /**
     * Handles the rising history produces non negative rising forecast step.
     */
    @Test
    void risingHistoryProducesNonNegativeRisingForecast() {
        // Holds the history for this calculation.
        List<TicketSalePoint> history = List.of(
                point(2, 2, 0), point(4, 6, 1), point(7, 13, 2), point(10, 23, 3)
        );

        // Holds the result for this calculation.
        ForecastResult result = new TimeSeriesForecaster().forecast("E001", history);

        assertEquals("RISING", result.getTrendLabel());
        assertTrue(result.getForecastNext24Hours() >= 0);
        assertTrue(result.getConfidence() >= 0.40 && result.getConfidence() <= 0.95);
    }

    /**
     * Handles the point step.
     */
    private TicketSalePoint point(int sold, int cumulative, int hour) {
        return new TicketSalePoint("E001", LocalDateTime.of(2026, 7, 1, hour, 0),
                800, 500, 100, sold, cumulative);
    }
}
