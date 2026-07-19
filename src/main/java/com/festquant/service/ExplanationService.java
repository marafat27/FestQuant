/**
 * Contains the explanation service implementation used by FestQuant.
 */
package com.festquant.service;

import com.festquant.domain.AuctionResult;

/**
 * Coordinates the business logic for explanation.
 */
public class ExplanationService {
    /**
     * Handles the explain auction step.
     */
    public String explainAuction(AuctionResult result) {
        if (result.getWinners().isEmpty()) {
            return "No premium seats were allocated because no valid bids were found.";
        }
        return result.getExplanation();
    }
}
