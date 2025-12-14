package com.college.docs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PendingAdminStore {
    private static final Path FILE = Paths.get("resources", "pending_admin_registrations.txt");

    private static void ensureExists() throws IOException {
        Path parent = FILE.getParent();
        if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
        if (!Files.exists(FILE)) Files.createFile(FILE);
    }

    public static synchronized void addPending(String email) {
        try {
            ensureExists();
            String line = email.trim().toLowerCase() + "," + Instant.now().toString() + System.lineSeparator();
            Files.write(FILE, line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ignore) {}
    }

    public static synchronized List<String> listPending() {
        try {
            ensureExists();
            return Files.readAllLines(FILE).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static synchronized void clearPendingFor(String email) {
        try {
            ensureExists();
            List<String> lines = Files.readAllLines(FILE);
            List<String> left = lines.stream().filter(l -> !l.toLowerCase().startsWith(email.trim().toLowerCase() + ",")).collect(Collectors.toList());
            Files.write(FILE, left);
        } catch (IOException ignore) {}
    }
}
