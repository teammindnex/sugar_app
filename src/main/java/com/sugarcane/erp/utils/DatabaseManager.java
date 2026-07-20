package com.sugarcane.erp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStream;
import java.util.Scanner;
import java.io.File;

public class DatabaseManager {

    // Define the DB path outside the JAR for persistence
    private static final String DB_DIR = "db";
    private static final String DB_FILE = "sugarcane_erp.db";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + File.separator + DB_FILE;

    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        // Enforce foreign keys when connecting to SQLite
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    private void initializeDatabase() {
        File dbDir = new File(DB_DIR);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        try (Connection conn = getConnection()) {
            InputStream is = getClass().getResourceAsStream("/db/schema.sql");
            if (is != null) {
                try (Scanner scanner = new Scanner(is, "UTF-8")) {
                    scanner.useDelimiter(";");
                    try (Statement stmt = conn.createStatement()) {
                        while (scanner.hasNext()) {
                            String sql = scanner.next().trim();
                            if (!sql.isEmpty()) {
                                stmt.execute(sql);
                            }
                        }
                    }
                }
            } else {
                System.err.println("Could not find schema.sql");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }
}
