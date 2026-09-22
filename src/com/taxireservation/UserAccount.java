package com.taxireservation;

/**
 * Simple authenticated user record used by the file-backed login flow.
 */
public class UserAccount {
    private final String username;
    private final String password;
    private final String role;
    private final String displayName;

    public UserAccount(String username, String password, String role, String displayName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }
}
