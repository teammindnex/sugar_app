package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.DailyReportItem;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

public class ReportDAO {

    public List<DailyReportItem> getDailyBuySellReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<LocalDate, DailyReportItem> map = new TreeMap<>(Collections.reverseOrder());

        String sqlPurchases = "SELECT purchase_date, weight, net_amount FROM Sugarcane_Purchases";
        String sqlSales = "SELECT sale_date, weight, net_amount FROM Sugarcane_Sales";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sqlPurchases)) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("purchase_date");
                    if (sqlDate != null) {
                        LocalDate d = sqlDate.toLocalDate();
                        if ((startDate == null || !d.isBefore(startDate)) && (endDate == null || !d.isAfter(endDate))) {
                            double wt = rs.getDouble("weight");
                            double amt = rs.getDouble("net_amount");
                            DailyReportItem item = map.computeIfAbsent(d, k -> new DailyReportItem(k, 0.0, 0.0, 0.0, 0.0));
                            item.setTotalPurchaseWeight(item.getTotalPurchaseWeight() + wt);
                            item.setTotalPurchaseAmount(item.getTotalPurchaseAmount() + amt);
                        }
                    }
                }
            }

            try (ResultSet rs = stmt.executeQuery(sqlSales)) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("sale_date");
                    if (sqlDate != null) {
                        LocalDate d = sqlDate.toLocalDate();
                        if ((startDate == null || !d.isBefore(startDate)) && (endDate == null || !d.isAfter(endDate))) {
                            double wt = rs.getDouble("weight");
                            double amt = rs.getDouble("net_amount");
                            DailyReportItem item = map.computeIfAbsent(d, k -> new DailyReportItem(k, 0.0, 0.0, 0.0, 0.0));
                            item.setTotalSaleWeight(item.getTotalSaleWeight() + wt);
                            item.setTotalSaleAmount(item.getTotalSaleAmount() + amt);
                        }
                    }
                }
            }
        }

        return new ArrayList<>(map.values());
    }
}
