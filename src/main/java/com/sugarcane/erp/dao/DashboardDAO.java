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
        String today = selectedDate.toString();
        String thisMonth = today.substring(0, 7); // yyyy-MM
        String thisYear = today.substring(0, 4);  // yyyy

        String sqlPurchase = "SELECT COALESCE(SUM(net_amount), 0) FROM Sugarcane_Purchases WHERE purchase_date = ?";
        String sqlSales = "SELECT COALESCE(SUM(net_amount), 0) FROM Sugarcane_Sales WHERE sale_date = ?";
        String sqlExpenses = "SELECT COALESCE(SUM(amount), 0) FROM Expenses WHERE expense_date = ?";
        String sqlCollection = "SELECT COALESCE(SUM(amount), 0) FROM Customer_Collections WHERE collection_date = ?";
        String sqlPayments = "SELECT COALESCE(SUM(amount), 0) FROM Farmer_Payments WHERE payment_date = ?";

        String sqlFarmers = "SELECT COUNT(*) FROM Farmers WHERE status = 'ACTIVE'";
        String sqlCustomers = "SELECT COUNT(*) FROM Customers WHERE status = 'ACTIVE'";
        String sqlWorkers = "SELECT COUNT(*) FROM Workers WHERE status = 'ACTIVE'";
        String sqlVehicles = "SELECT COUNT(*) FROM Transports WHERE status = 'ACTIVE'";
        
        String sqlMonthlyPurchaseWt = "SELECT COALESCE(SUM(weight), 0) FROM Sugarcane_Purchases WHERE strftime('%Y-%m', purchase_date) = ?";
        String sqlMonthlySalesWt = "SELECT COALESCE(SUM(weight), 0) FROM Sugarcane_Sales WHERE strftime('%Y-%m', sale_date) = ?";
        String sqlYearlyPurchaseWt = "SELECT COALESCE(SUM(weight), 0) FROM Sugarcane_Purchases WHERE strftime('%Y', purchase_date) = ?";
        String sqlYearlySalesWt = "SELECT COALESCE(SUM(weight), 0) FROM Sugarcane_Sales WHERE strftime('%Y', sale_date) = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            metrics.setTodayPurchase(getDouble(conn, sqlPurchase, today));
            metrics.setTodaySales(getDouble(conn, sqlSales, today));
            metrics.setTodayExpenses(getDouble(conn, sqlExpenses, today));
            metrics.setTodayCollection(getDouble(conn, sqlCollection, today));
            metrics.setTodayPayments(getDouble(conn, sqlPayments, today));

            metrics.setTotalFarmers(getInt(conn, sqlFarmers));
            metrics.setTotalCustomers(getInt(conn, sqlCustomers));
            metrics.setTotalWorkers(getInt(conn, sqlWorkers));
            metrics.setTotalVehicles(getInt(conn, sqlVehicles));
            
            metrics.setMonthlyPurchaseWeight(getDouble(conn, sqlMonthlyPurchaseWt, thisMonth));
            metrics.setMonthlySalesWeight(getDouble(conn, sqlMonthlySalesWt, thisMonth));
            metrics.setYearlyPurchaseWeight(getDouble(conn, sqlYearlyPurchaseWt, thisYear));
            metrics.setYearlySalesWeight(getDouble(conn, sqlYearlySalesWt, thisYear));
        }

        return metrics;
    }

    private double getDouble(Connection conn, String sql, String dateStr) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dateStr);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    private int getInt(Connection conn, String sql) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
