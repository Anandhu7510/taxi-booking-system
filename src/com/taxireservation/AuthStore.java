package com.taxireservation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Very small file-backed authentication store for the microproject.
 * Format: username|password|role|displayName
 */
public final class AuthStore {
    private static final Path USER_FILE = Paths.get("data", "users.txt");

    private AuthStore() { }

    public static synchronized void ensureStorage() throws IOException {
        Path parent = USER_FILE.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(USER_FILE)) {
            Files.createFile(USER_FILE);
        }
    }

    public static synchronized UserAccount authenticate(String username, String password, String requiredRole)
            throws IOException {
        ensureStorage();
        BufferedReader reader = Files.newBufferedReader(USER_FILE, StandardCharsets.UTF_8);
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                UserAccount account = parse(line);
                if (account == null) continue;
                if (account.getUsername().equalsIgnoreCase(username.trim())
                        && account.getPassword().equals(password)
                        && account.getRole().equalsIgnoreCase(requiredRole)) {
                    return account;
                }
            }
        } finally {
            reader.close();
        }
        return null;
    }

    public static synchronized boolean usernameExists(String username) throws IOException {
        ensureStorage();
        BufferedReader reader = Files.newBufferedReader(USER_FILE, StandardCharsets.UTF_8);
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                UserAccount account = parse(line);
                if (account != null && account.getUsername().equalsIgnoreCase(username.trim())) {
                    return true;
                }
            }
        } finally {
            reader.close();
        }
        return false;
    }

    public static synchronized UserAccount registerCustomer(String displayName, String username, String password)
            throws IOException {
        ensureStorage();
        if (usernameExists(username)) {
            return null;
        }

        UserAccount account = new UserAccount(username.trim(), password, "CUSTOMER", displayName.trim());
        BufferedWriter writer = Files.newBufferedWriter(USER_FILE, StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
        try {
            writer.write(serialize(account));
            writer.newLine();
        } finally {
            writer.close();
        }
        return account;
    }

    private static UserAccount parse(String line) {
        if (line == null) return null;
        String trimmed = line.trim();
        if (trimmed.length() == 0 || trimmed.startsWith("#")) return null;

        String[] parts = trimmed.split("\\|", -1);
        if (parts.length != 4) return null;
        return new UserAccount(parts[0], parts[1], parts[2], parts[3]);
    }

    private static String serialize(UserAccount account) {
        return safe(account.getUsername()) + "|" + safe(account.getPassword()) + "|"
                + safe(account.getRole()) + "|" + safe(account.getDisplayName());
    }

    private static String safe(String value) {
        return value.replace("|", "").replace("\n", " ").replace("\r", " ");
    }
}
