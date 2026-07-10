package com.festquant;

import com.festquant.repository.CsvBidRepository;
import com.festquant.service.DataValidationService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CsvBidRepositoryTest {
    @Test
    void loadsBidsFromCsv() {
        CsvBidRepository repository = new CsvBidRepository(
                Path.of("data/input/premium_bids.csv"),
                new DataValidationService()
        );

        assertFalse(repository.findBidsByEventId("E001").isEmpty());
    }
}
