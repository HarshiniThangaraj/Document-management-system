package com.college.docs;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VerifiedAdminStore {
    private static final Path STORE = Paths.get("resources", "verified_admins.txt");

    private static void ensureStoreExists() throws IOException {
        Path parent = STORE.getParent();
        if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
        if (!Files.exists(STORE)) Files.createFile(STORE);
    }

    public static synchronized Set<String> listVerified() {
        try {
            ensureStoreExists();
            List<String> lines = Files.readAllLines(STORE).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            return new HashSet<>(lines);
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    public static synchronized boolean isVerified(String email) {
        if (email == null) return false;
        Set<String> set = listVerified();
        return set.contains(email.trim().toLowerCase());
    }

    public static synchronized void addVerified(String email) throws IOException {
        if (email == null || email.trim().isEmpty()) return;
        Set<String> set = listVerified();
        if (set.add(email.trim().toLowerCase())) {
            Files.write(STORE, (set.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator()).getBytes());
        }
    }

    public static synchronized void removeVerified(String email) throws IOException {
        if (email == null || email.trim().isEmpty()) return;
        Set<String> set = listVerified();
        if (set.remove(email.trim().toLowerCase())) {
            Files.write(STORE, (set.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator()).getBytes());
        }
    }
}
