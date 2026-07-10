package com.festquant.service;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionBidView;
import com.festquant.domain.Event;
import com.festquant.domain.PriceRecommendation;
import com.festquant.model.ForecastResult;
import com.festquant.pricing.PriceSimulationRow;

import java.util.List;
import java.util.Map;

/**
 * Facade for every use case exposed to a UI. Controllers depend on this
 * abstraction rather than concrete repositories or algorithms.
 */
public interface AnalyticsFacade {
    List<Event> events();
    List<ForecastResult> forecasts();
    List<PriceRecommendation> recommendations();
    List<PriceRecommendation> refreshRecommendations();
    List<PriceSimulationRow> simulations(String eventId);
    List<AuctionBidView> auctionBids(String eventId);
    AuctionResult runAuction(String eventId);
    AuctionResult runVickreyAuction(String eventId);
    AuctionResult auctionResult(String eventId);
    Map<String, Object> dashboardSummary();
}
