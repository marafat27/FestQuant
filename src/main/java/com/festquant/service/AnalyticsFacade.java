/**
 * Contains the analytics facade implementation used by FestQuant.
 */
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
    /**
     * Handles the events step.
     */
    List<Event> events();
    /**
     * Handles the forecasts step.
     */
    List<ForecastResult> forecasts();
    /**
     * Recommends ations.
     */
    List<PriceRecommendation> recommendations();
    /**
     * Handles the refresh recommendations step.
     */
    List<PriceRecommendation> refreshRecommendations();
    /**
     * Handles the simulations step.
     */
    List<PriceSimulationRow> simulations(String eventId);
    /**
     * Handles the auction bids step.
     */
    List<AuctionBidView> auctionBids(String eventId);
    /**
     * Runs auction.
     */
    AuctionResult runAuction(String eventId);
    /**
     * Runs vickrey auction.
     */
    AuctionResult runVickreyAuction(String eventId);
    /**
     * Handles the auction result step.
     */
    AuctionResult auctionResult(String eventId);
    Map<String, Object> dashboardSummary();
}
