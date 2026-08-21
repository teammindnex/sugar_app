package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.Customer;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public int addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customers (name, mobile, village, address, gst, opening_balance, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getMobile());
            pstmt.setString(3, customer.getVillage());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getGst());
            pstmt.setDouble(6, customer.getOpeningBalance());
            pstmt.setString(7, customer.getStatus());
            
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customers ORDER BY name ASC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
        }
        return list;
    }
    
    public void updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE Customers SET name=?, mobile=?, village=?, address=?, gst=?, " +
                     "opening_balance=?, status=?, updated_at=CURRENT_TIMESTAMP " +
                     "WHERE id=?";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getMobile());
            pstmt.setString(3, customer.getVillage());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getGst());
            pstmt.setDouble(6, customer.getOpeningBalance());
            pstmt.setString(7, customer.getStatus());
            pstmt.setInt(8, customer.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    public void deleteCustomer(int id) throws SQLException {
        String sql = "DELETE FROM Customers WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("mobile"),
            rs.getString("village"),
            rs.getString("address"),
            rs.getString("gst"),
            rs.getDouble("opening_balance"),
            rs.getString("status")
        );
    }
}
