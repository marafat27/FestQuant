/**
 * Contains the export service implementation used by FestQuant.
 */
package com.festquant.service;

import com.festquant.domain.AuctionResult;
import com.festquant.util.JsonExporter;

import java.nio.file.Path;

/**
 * Coordinates the business logic for export.
 */
public class ExportService {
    // Stores the json exporter used by this class.
    private final JsonExporter jsonExporter;

    /**
     * Creates a ExportService with the values needed by this component.
     */
    public ExportService(JsonExporter jsonExporter) {
        this.jsonExporter = jsonExporter;
    }

    /**
     * Exports auction result.
     */
    public Path exportAuctionResult(AuctionResult result, Path outputPath) {
        jsonExporter.exportAuctionResult(result, outputPath);
        return outputPath;
    }
}
