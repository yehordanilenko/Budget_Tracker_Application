package org.ydanilenko.budgettracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Directly set the database URL to use a relative path
    private static final String DATABASE_URL = "jdbc:sqlite:budgettracker.db";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
