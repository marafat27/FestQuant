/**
 * Contains the forecast service implementation used by FestQuant.
 */
package com.festquant.service;

import com.festquant.domain.PricingEvent;
import com.festquant.domain.TicketSalePoint;
import com.festquant.model.ForecastResult;
import com.festquant.model.TimeSeriesForecaster;
import com.festquant.repository.CsvPricingEventRepository;
import com.festquant.repository.CsvSalesRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Coordinates parallel Holt forecasts while keeping the model framework-free. */
public final class ForecastService {
    // Stores the event repository used by this class.
    private final CsvPricingEventRepository eventRepository;
    // Stores the sales repository used by this class.
    private final CsvSalesRepository salesRepository;
    // Stores the forecaster used by this class.
    private final TimeSeriesForecaster forecaster;

    /**
     * Creates a ForecastService with the values needed by this component.
     */
    public ForecastService(CsvPricingEventRepository eventRepository,
                           CsvSalesRepository salesRepository,
                           TimeSeriesForecaster forecaster) {
        this.eventRepository = eventRepository;
        this.salesRepository = salesRepository;
        this.forecaster = forecaster;
    }

    /**
     * Handles the forecast all step.
     */
    public List<ForecastResult> forecastAll() {
        Map<String, List<TicketSalePoint>> history = salesRepository.groupByEvent();
        return eventRepository.findAll().parallelStream()
                .map(PricingEvent::getEventId)
                .map(id -> forecaster.forecast(id, history.getOrDefault(id, List.of())))
                .sorted(Comparator.comparing(ForecastResult::getEventId))
                .toList();
    }
}
