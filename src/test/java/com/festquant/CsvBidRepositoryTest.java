/**
 * Contains the csv bid repository test implementation used by FestQuant.
 */
package com.festquant;

import com.festquant.repository.CsvBidRepository;
import com.festquant.service.DataValidationService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Checks the expected behaviour of csv bid repository.
 */
class CsvBidRepositoryTest {
    /**
     * Loads s bids from csv.
     */
    @Test
    void loadsBidsFromCsv() {
        // Holds the repository for this calculation.
        CsvBidRepository repository = new CsvBidRepository(
                Path.of("data/input/premium_bids.csv"),
                new DataValidationService()
        );

        assertFalse(repository.findBidsByEventId("E001").isEmpty());
    }
}
