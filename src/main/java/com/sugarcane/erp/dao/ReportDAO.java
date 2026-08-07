package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.DailyReportItem;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public List<DailyReportItem> getDailyBuySellReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<DailyReportItem> list = new ArrayList<>();
        
        String sql = "SELECT " +
                     "    d.report_date, " +
                     "    COALESCE(p.total_weight, 0) as purchase_weight, " +
                     "    COALESCE(p.total_amount, 0) as purchase_amount, " +
                     "    COALESCE(s.total_weight, 0) as sale_weight, " +
                     "    COALESCE(s.total_amount, 0) as sale_amount " +
                     "FROM (" +
                     "    SELECT purchase_date as report_date FROM Sugarcane_Purchases WHERE purchase_date BETWEEN ? AND ? " +
                     "    UNION " +
                     "    SELECT sale_date as report_date FROM Sugarcane_Sales WHERE sale_date BETWEEN ? AND ? " +
                     ") d " +
                     "LEFT JOIN (" +
                     "    SELECT purchase_date, SUM(weight) as total_weight, SUM(net_amount) as total_amount " +
                     "    FROM Sugarcane_Purchases " +
                     "    WHERE purchase_date BETWEEN ? AND ? " +
                     "    GROUP BY purchase_date" +
                     ") p ON d.report_date = p.purchase_date " +
                     "LEFT JOIN (" +
                     "    SELECT sale_date, SUM(weight) as total_weight, SUM(net_amount) as total_amount " +
                     "    FROM Sugarcane_Sales " +
                     "    WHERE sale_date BETWEEN ? AND ? " +
                     "    GROUP BY sale_date" +
                     ") s ON d.report_date = s.sale_date " +
                     "ORDER BY d.report_date DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            String start = startDate.toString();
            String end = endDate.toString();
            
            // Set for UNION subquery
            pstmt.setString(1, start);
            pstmt.setString(2, end);
            pstmt.setString(3, start);
            pstmt.setString(4, end);
            
            // Set for purchase JOIN subquery
            pstmt.setString(5, start);
            pstmt.setString(6, end);
            
            // Set for sale JOIN subquery
            pstmt.setString(7, start);
            pstmt.setString(8, end);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                DailyReportItem item = new DailyReportItem(
                    LocalDate.parse(rs.getString("report_date")),
                    rs.getDouble("purchase_weight"),
                    rs.getDouble("purchase_amount"),
                    rs.getDouble("sale_weight"),
                    rs.getDouble("sale_amount")
                );
                list.add(item);
            }
        }
        return list;
    }
}
