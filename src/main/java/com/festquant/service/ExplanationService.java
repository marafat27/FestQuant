package com.festquant.service;

import com.festquant.domain.AuctionResult;

public class ExplanationService {
    public String explainAuction(AuctionResult result) {
        if (result.getWinners().isEmpty()) {
            return "No premium seats were allocated because no valid bids were found.";
        }
        return result.getExplanation();
    }
}
