package com.sugarcane.erp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MigrateDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:db/sugarcane_erp.db";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            
            String sql = "ALTER TABLE Sugarcane_Purchases ADD COLUMN bill_no TEXT;";
            stmt.execute(sql);
            System.out.println("Migration successful!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
