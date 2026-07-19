/**
 * Spring MVC controllers that expose the FestQuant use cases as REST APIs.
 *
 * <p>Controllers intentionally stay thin: they validate URL parameters, call
 * the application facade, and return DTO/domain objects to the browser. The
 * forecasting, pricing, auction and live-stream rules remain in the service
 * layer so the same logic can be reused by tests and alternative UIs.</p>
 */
package com.festquant.controller;
