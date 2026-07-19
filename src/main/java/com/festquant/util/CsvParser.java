/**
 * Contains the csv parser implementation used by FestQuant.
 */
package com.festquant.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the csv parser part of the FestQuant application.
 */
public final class CsvParser {
    /**
     * Creates a CsvParser with the values needed by this component.
     */
    private CsvParser() {
    }

    /**
     * Handles the parse line step.
     */
    public static List<String> parseLine(String line) {
        // Holds the values for this calculation.
        List<String> values = new ArrayList<>();
        // Holds the current for this calculation.
        StringBuilder current = new StringBuilder();
        // Holds the in quotes for this calculation.
        boolean inQuotes = false;

        // Uses i for the current item in the loop.
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString().trim());
        return values;
    }
}
