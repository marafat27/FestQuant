package com.festquant.service;

import com.festquant.domain.AuctionResult;
import com.festquant.util.JsonExporter;

import java.nio.file.Path;

public class ExportService {
    private final JsonExporter jsonExporter;

    public ExportService(JsonExporter jsonExporter) {
        this.jsonExporter = jsonExporter;
    }

    public Path exportAuctionResult(AuctionResult result, Path outputPath) {
        jsonExporter.exportAuctionResult(result, outputPath);
        return outputPath;
    }
}
