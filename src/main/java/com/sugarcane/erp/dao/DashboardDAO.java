package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.DashboardMetrics;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DashboardDAO {

    public DashboardMetrics getMetrics(LocalDate selectedDate) throws SQLException {
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }
        DashboardMetrics metrics = new DashboardMetrics();
        int selectedYear = selectedDate.getYear();
        int selectedMonth = selectedDate.getMonthValue();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            // Farmers, Customers, Workers, Vehicles
            metrics.setTotalFarmers(getInt(conn, "SELECT COUNT(*) FROM Farmers WHERE status = 'ACTIVE'"));
            metrics.setTotalCustomers(getInt(conn, "SELECT COUNT(*) FROM Customers WHERE status = 'ACTIVE'"));
            metrics.setTotalWorkers(getInt(conn, "SELECT COUNT(*) FROM Workers WHERE status = 'ACTIVE'"));
            metrics.setTotalVehicles(getInt(conn, "SELECT COUNT(*) FROM Transports WHERE status = 'ACTIVE'"));

            // Purchases
            try (ResultSet rs = stmt.executeQuery("SELECT purchase_date, net_amount, weight FROM Sugarcane_Purchases")) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("purchase_date");
                    if (sqlDate != null) {
                        LocalDate d = sqlDate.toLocalDate();
                        double netAmt = rs.getDouble("net_amount");
                        double wt = rs.getDouble("weight");
                        
                        if (d.equals(selectedDate)) {
                            metrics.setTodayPurchase(metrics.getTodayPurchase() + netAmt);
                            metrics.setTodayPurchaseWeight(metrics.getTodayPurchaseWeight() + wt);
                        }
                        if (d.getYear() == selectedYear && d.getMonthValue() == selectedMonth) {
                            metrics.setMonthlyPurchaseWeight(metrics.getMonthlyPurchaseWeight() + wt);
                        }
                        if (d.getYear() == selectedYear) {
                            metrics.setYearlyPurchaseWeight(metrics.getYearlyPurchaseWeight() + wt);
                        }
                    }
                }
            }

            // Sales
            try (ResultSet rs = stmt.executeQuery("SELECT sale_date, net_amount, weight FROM Sugarcane_Sales")) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("sale_date");
                    if (sqlDate != null) {
                        LocalDate d = sqlDate.toLocalDate();
                        double netAmt = rs.getDouble("net_amount");
                        double wt = rs.getDouble("weight");
                        
                        if (d.equals(selectedDate)) {
                            metrics.setTodaySales(metrics.getTodaySales() + netAmt);
                            metrics.setTodaySalesWeight(metrics.getTodaySalesWeight() + wt);
                        }
                        if (d.getYear() == selectedYear && d.getMonthValue() == selectedMonth) {
                            metrics.setMonthlySalesWeight(metrics.getMonthlySalesWeight() + wt);
                        }
                        if (d.getYear() == selectedYear) {
                            metrics.setYearlySalesWeight(metrics.getYearlySalesWeight() + wt);
                        }
                    }
                }
            }

            // Expenses
            try (ResultSet rs = stmt.executeQuery("SELECT expense_date, amount FROM Expenses")) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("expense_date");
                    if (sqlDate != null && sqlDate.toLocalDate().equals(selectedDate)) {
                        metrics.setTodayExpenses(metrics.getTodayExpenses() + rs.getDouble("amount"));
                    }
                }
            }

            // Collections
            try (ResultSet rs = stmt.executeQuery("SELECT collection_date, amount FROM Customer_Collections")) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("collection_date");
                    if (sqlDate != null && sqlDate.toLocalDate().equals(selectedDate)) {
                        metrics.setTodayCollection(metrics.getTodayCollection() + rs.getDouble("amount"));
                    }
                }
            }

            // Payments
            try (ResultSet rs = stmt.executeQuery("SELECT payment_date, amount FROM Farmer_Payments")) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("payment_date");
                    if (sqlDate != null && sqlDate.toLocalDate().equals(selectedDate)) {
                        metrics.setTodayPayments(metrics.getTodayPayments() + rs.getDouble("amount"));
                    }
                }
            }
        }

        return metrics;
    }

    private int getInt(Connection conn, String sql) throws SQLException {
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
