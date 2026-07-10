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
    private final CsvPricingEventRepository eventRepository;
    private final CsvSalesRepository salesRepository;
    private final TimeSeriesForecaster forecaster;

    public ForecastService(CsvPricingEventRepository eventRepository,
                           CsvSalesRepository salesRepository,
                           TimeSeriesForecaster forecaster) {
        this.eventRepository = eventRepository;
        this.salesRepository = salesRepository;
        this.forecaster = forecaster;
    }

    public List<ForecastResult> forecastAll() {
        Map<String, List<TicketSalePoint>> history = salesRepository.groupByEvent();
        return eventRepository.findAll().parallelStream()
                .map(PricingEvent::getEventId)
                .map(id -> forecaster.forecast(id, history.getOrDefault(id, List.of())))
                .sorted(Comparator.comparing(ForecastResult::getEventId))
                .toList();
    }
}
