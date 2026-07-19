/**
 * Contains the csv user repository implementation used by FestQuant.
 */
package com.festquant.repository;

import com.festquant.domain.Admin;
import com.festquant.domain.Participant;
import com.festquant.domain.User;
import com.festquant.domain.UserType;
import com.festquant.exception.DataLoadException;
import com.festquant.util.CsvParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads and stores csv user data.
 */
public class CsvUserRepository implements Repository<User> {
    // Stores the csv path used by this class.
    private final Path csvPath;

    /**
     * Creates a CsvUserRepository with the values needed by this component.
     */
    public CsvUserRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    /**
     * Finds all.
     */
    @Override
    public List<User> findAll() {
        // Holds the users for this calculation.
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                // Holds the row for this calculation.
                List<String> row = CsvParser.parseLine(line);
                // Holds the type for this calculation.
                UserType type = UserType.valueOf(row.get(2));
                // Holds the wallet balance for this calculation.
                double walletBalance = Double.parseDouble(row.get(3));
                if (type == UserType.ADMIN) {
                    users.add(new Admin(row.get(0), row.get(1), walletBalance));
                } else {
                    users.add(new Participant(row.get(0), row.get(1), walletBalance));
                }
            }
            return users;
        } catch (IOException | RuntimeException ex) {
            throw new DataLoadException("Unable to load users from " + csvPath, ex);
        }
    }

    /**
     * Finds by id.
     */
    @Override
    public Optional<User> findById(String id) {
        return findAll().stream().filter(user -> user.getUserId().equals(id)).findFirst();
    }
}
