package com.college.docs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignmentStore {
    private static final Path STORE = Paths.get("resources", "assignments.txt");

    private static void ensureStore() throws IOException {
        Path parent = STORE.getParent();
        if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
        if (!Files.exists(STORE)) Files.createFile(STORE);
    }

    public static synchronized Map<Integer, String> loadAll() {
        try {
            ensureStore();
            List<String> lines = Files.readAllLines(STORE, StandardCharsets.UTF_8);
            Map<Integer, String> map = new HashMap<>();
            for (String l : lines) {
                if (l == null || l.trim().isEmpty()) continue;
                String[] parts = l.split("\\|", 2);
                if (parts.length != 2) continue;
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    map.put(id, parts[1].trim().toLowerCase());
                } catch (NumberFormatException ignore) {}
            }
            return map;
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    public static synchronized String getAssignedAdmin(int docId) {
        Map<Integer, String> map = loadAll();
        return map.getOrDefault(docId, "");
    }

    public static synchronized void setAssignedAdmin(int docId, String adminEmail) throws IOException {
        Map<Integer, String> map = loadAll();
        if (adminEmail == null || adminEmail.trim().isEmpty()) map.remove(docId);
        else map.put(docId, adminEmail.trim().toLowerCase());
        // write out
        List<String> lines = map.entrySet().stream()
                .map(e -> e.getKey() + "|" + e.getValue())
                .collect(Collectors.toList());
        Files.write(STORE, lines, StandardCharsets.UTF_8);
    }

    public static synchronized void removeAssignment(int docId) throws IOException {
        setAssignedAdmin(docId, "");
    }
}
