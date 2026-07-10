/**
 * Data-access adapters for CSV files and the embedded H2 database.
 *
 * <p>The repositories demonstrate dependency inversion: services depend on
 * repository contracts and focused adapter classes instead of knowing how CSV
 * parsing or JDBC persistence works. That separation makes the business layer
 * easier to test and replace.</p>
 */
package com.festquant.repository;
