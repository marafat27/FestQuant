/**
 * Application services that coordinate complete business workflows.
 *
 * <p>This layer connects repositories, forecasters, pricing strategies,
 * auctions, exporters and the live-demand stream. {@code FestQuantEngine}
 * acts as a facade so the UI and controllers can call one clean interface
 * instead of assembling the full pipeline themselves.</p>
 */
package com.festquant.service;
