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

public class CsvUserRepository implements Repository<User> {
    private final Path csvPath;

    public CsvUserRepository(Path csvPath) {
        this.csvPath = csvPath;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> row = CsvParser.parseLine(line);
                UserType type = UserType.valueOf(row.get(2));
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

    @Override
    public Optional<User> findById(String id) {
        return findAll().stream().filter(user -> user.getUserId().equals(id)).findFirst();
    }
}
