/**
 * Forecasting and regression support for demand prediction.
 *
 * <p>The model package combines Holt-style time-series forecasting with a
 * Java adapter for the nonlinear logistic regression coefficients trained from
 * the generated festival dataset. The pricing layer uses these predictions to
 * estimate candidate-ticket demand before choosing a revenue-maximising price.</p>
 */
package com.festquant.model;
