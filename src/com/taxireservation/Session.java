package com.taxireservation;

/**
 * Holds the currently authenticated user for the lifetime of the application.
 */
public final class Session {
    private static UserAccount currentUser;

    private Session() { }

    public static void login(UserAccount user) {
        currentUser = user;
    }

    public static UserAccount getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equalsIgnoreCase(role);
    }

    public static void logout() {
        currentUser = null;
    }
}
