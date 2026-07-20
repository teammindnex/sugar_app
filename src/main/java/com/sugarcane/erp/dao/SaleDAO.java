package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.Sale;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public void addSale(Sale sale) throws SQLException {
        String sql = "INSERT INTO Sugarcane_Sales (customer_id, sale_date, cane_type, vehicle_no, weight, " +
                     "rate_per_ton, total_amount, received_amount, net_amount, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sale.getCustomerId());
            pstmt.setDate(2, Date.valueOf(sale.getSaleDate()));
            pstmt.setString(3, sale.getCaneType());
            pstmt.setString(4, sale.getVehicleNo());
            pstmt.setDouble(5, sale.getWeight());
            pstmt.setDouble(6, sale.getRatePerTon());
            pstmt.setDouble(7, sale.getTotalAmount());
            pstmt.setDouble(8, sale.getReceivedAmount());
            pstmt.setDouble(9, sale.getNetAmount());
            pstmt.setString(10, sale.getRemarks());
            
            pstmt.executeUpdate();
        }
    }

    public List<Sale> getAllSales() throws SQLException {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.*, c.name as customer_name FROM Sugarcane_Sales s " +
                     "JOIN Customers c ON s.customer_id = c.id ORDER BY s.sale_date DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToSale(rs));
            }
        }
        return list;
    }
    
    public void deleteSale(int id) throws SQLException {
        String sql = "DELETE FROM Sugarcane_Sales WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setId(rs.getInt("id"));
        s.setCustomerId(rs.getInt("customer_id"));
        s.setCustomerName(rs.getString("customer_name"));
        s.setSaleDate(rs.getDate("sale_date").toLocalDate());
        s.setCaneType(rs.getString("cane_type"));
        s.setVehicleNo(rs.getString("vehicle_no"));
        s.setWeight(rs.getDouble("weight"));
        s.setRatePerTon(rs.getDouble("rate_per_ton"));
        s.setTotalAmount(rs.getDouble("total_amount"));
        s.setReceivedAmount(rs.getDouble("received_amount"));
        s.setNetAmount(rs.getDouble("net_amount"));
        s.setRemarks(rs.getString("remarks"));
        return s;
    }
}
