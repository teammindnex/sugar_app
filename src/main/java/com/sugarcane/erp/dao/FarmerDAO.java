package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FarmerDAO {

    public void addFarmer(Farmer farmer) throws SQLException {
        String sql = "INSERT INTO Farmers (name, mobile, village, taluka, district, address, " +
                     "aadhar_number, bank_details, opening_balance, remarks, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, farmer.getName());
            pstmt.setString(2, farmer.getMobile());
            pstmt.setString(3, farmer.getVillage());
            pstmt.setString(4, farmer.getTaluka());
            pstmt.setString(5, farmer.getDistrict());
            pstmt.setString(6, farmer.getAddress());
            pstmt.setString(7, farmer.getAadharNumber());
            pstmt.setString(8, farmer.getBankDetails());
            pstmt.setDouble(9, farmer.getOpeningBalance());
            pstmt.setString(10, farmer.getRemarks());
            pstmt.setString(11, farmer.getStatus());
            
            pstmt.executeUpdate();
        }
    }

    public List<Farmer> getAllFarmers() throws SQLException {
        List<Farmer> list = new ArrayList<>();
        String sql = "SELECT * FROM Farmers ORDER BY name ASC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToFarmer(rs));
            }
        }
        return list;
    }
    
    public void updateFarmer(Farmer farmer) throws SQLException {
        String sql = "UPDATE Farmers SET name=?, mobile=?, village=?, taluka=?, district=?, address=?, " +
                     "aadhar_number=?, bank_details=?, opening_balance=?, remarks=?, status=?, updated_at=CURRENT_TIMESTAMP " +
                     "WHERE id=?";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, farmer.getName());
            pstmt.setString(2, farmer.getMobile());
            pstmt.setString(3, farmer.getVillage());
            pstmt.setString(4, farmer.getTaluka());
            pstmt.setString(5, farmer.getDistrict());
            pstmt.setString(6, farmer.getAddress());
            pstmt.setString(7, farmer.getAadharNumber());
            pstmt.setString(8, farmer.getBankDetails());
            pstmt.setDouble(9, farmer.getOpeningBalance());
            pstmt.setString(10, farmer.getRemarks());
            pstmt.setString(11, farmer.getStatus());
            pstmt.setInt(12, farmer.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    public void deleteFarmer(int id) throws SQLException {
        String sql = "DELETE FROM Farmers WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Farmer mapResultSetToFarmer(ResultSet rs) throws SQLException {
        return new Farmer(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("mobile"),
            rs.getString("village"),
            rs.getString("taluka"),
            rs.getString("district"),
            rs.getString("address"),
            rs.getString("aadhar_number"),
            rs.getString("bank_details"),
            rs.getDouble("opening_balance"),
            rs.getString("remarks"),
            rs.getString("status")
        );
    }
}
