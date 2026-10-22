package com.taxireservation;

import java.sql.SQLException;

/** JDBC-backed authentication facade retained for existing Swing forms. */
public final class AuthStore {
    private AuthStore() {}
    public static UserAccount authenticate(String username, String password, String requiredRole)
            throws SQLException { return UserDAO.authenticate(username, password, requiredRole); }
    public static boolean usernameExists(String username) throws SQLException {
        return UserDAO.usernameExists(username);
    }
    public static UserAccount registerCustomer(String displayName, String username, String password)
            throws SQLException { return UserDAO.register(displayName, username, password); }
    public static void ensureAdministrator() throws SQLException {
        UserDAO.ensureAdministrator();
    }
}
