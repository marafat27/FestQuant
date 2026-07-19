/**
 * Live-market simulation classes used by the frontend demonstration.
 *
 * <p>A scheduled producer creates compressed-time ticket activity, a consumer
 * maintains rolling demand windows, and Server-Sent Events push updated demand
 * and price snapshots to the browser. This package highlights Java concurrency
 * tools while keeping prices inside the same economic constraints as the
 * historical pricing model.</p>
 */
package com.festquant.stream;
