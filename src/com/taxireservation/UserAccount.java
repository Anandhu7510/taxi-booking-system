package com.taxireservation;

/** Session identity; password hashes are never kept in a session object. */
public final class UserAccount {
    private final int id;
    private final String username;
    private final String role;
    private final String displayName;

    public UserAccount(int id, String username, String role, String displayName) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.displayName = displayName;
    }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }
}
