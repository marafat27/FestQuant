/**
 * Contains the live demand snapshot implementation used by FestQuant.
 */
package com.festquant.stream;

import java.time.Instant;

/** Current rolling-window state delivered to the web dashboard over SSE. */
public record LiveDemandSnapshot(
        String eventId,
        String eventName,
        Instant observedAt,
        long sequence,
        int latestDemand,
        double rollingDemand,
        int views,
        int wishlists,
        double livePrice,
        double basePrice,
        double priceChangePercent,
        String trend,
        String scenario,
        boolean running
) {
}
