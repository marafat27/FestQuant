package com.festquant.stream;

import java.time.Instant;

/** Immutable event placed on the producer-consumer queue. */
public record LiveTicketSale(
        String eventId,
        Instant observedAt,
        long sequence,
        int views,
        int wishlists,
        int ticketsSold
) {
}
