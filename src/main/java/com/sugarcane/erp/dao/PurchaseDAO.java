package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.Purchase;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public void addPurchase(Purchase purchase) throws SQLException {
        String sql = "INSERT INTO Sugarcane_Purchases (farmer_id, bill_no, purchase_date, cane_type, vehicle_no, empty_weight, loaded_weight, weight, " +
                     "rate_per_ton, total_amount, advance, loading_charges, cutting_charges, transport_charges, " +
                     "other_charges, net_amount, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, purchase.getFarmerId());
            pstmt.setString(2, purchase.getBillNo());
            pstmt.setDate(3, Date.valueOf(purchase.getPurchaseDate()));
            pstmt.setString(4, purchase.getCaneType());
            pstmt.setString(5, purchase.getVehicleNo());
            pstmt.setDouble(6, purchase.getEmptyWeight());
            pstmt.setDouble(7, purchase.getLoadedWeight());
            pstmt.setDouble(8, purchase.getWeight());
            pstmt.setDouble(9, purchase.getRatePerTon());
            pstmt.setDouble(10, purchase.getTotalAmount());
            pstmt.setDouble(11, purchase.getAdvance());
            pstmt.setDouble(12, purchase.getLoadingCharges());
            pstmt.setDouble(13, purchase.getCuttingCharges());
            pstmt.setDouble(14, purchase.getTransportCharges());
            pstmt.setDouble(15, purchase.getOtherCharges());
            pstmt.setDouble(16, purchase.getNetAmount());
            pstmt.setString(17, purchase.getRemarks());
            
            pstmt.executeUpdate();
        }
    }

    public List<Purchase> getAllPurchases() throws SQLException {
        List<Purchase> list = new ArrayList<>();
        String sql = "SELECT p.*, f.name as farmer_name FROM Sugarcane_Purchases p " +
                     "JOIN Farmers f ON p.farmer_id = f.id ORDER BY p.purchase_date DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToPurchase(rs));
            }
        }
        return list;
    }
    
    public void deletePurchase(int id) throws SQLException {
        String sql = "DELETE FROM Sugarcane_Purchases WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public String getNextBillNo() throws SQLException {
        String sql = "SELECT MAX(id) FROM Sugarcane_Purchases";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int nextId = rs.getInt(1) + 1;
                return "FRM-" + java.time.Year.now().getValue() + "-" + String.format("%04d", nextId);
            }
        }
        return "FRM-" + java.time.Year.now().getValue() + "-0001";
    }

    private Purchase mapResultSetToPurchase(ResultSet rs) throws SQLException {
        Purchase p = new Purchase();
        p.setId(rs.getInt("id"));
        p.setFarmerId(rs.getInt("farmer_id"));
        p.setFarmerName(rs.getString("farmer_name"));
        p.setBillNo(rs.getString("bill_no"));
        p.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
        p.setCaneType(rs.getString("cane_type"));
        p.setVehicleNo(rs.getString("vehicle_no"));
        p.setEmptyWeight(rs.getDouble("empty_weight"));
        p.setLoadedWeight(rs.getDouble("loaded_weight"));
        p.setWeight(rs.getDouble("weight"));
        p.setRatePerTon(rs.getDouble("rate_per_ton"));
        p.setTotalAmount(rs.getDouble("total_amount"));
        p.setAdvance(rs.getDouble("advance"));
        p.setLoadingCharges(rs.getDouble("loading_charges"));
        p.setCuttingCharges(rs.getDouble("cutting_charges"));
        p.setTransportCharges(rs.getDouble("transport_charges"));
        p.setOtherCharges(rs.getDouble("other_charges"));
        p.setNetAmount(rs.getDouble("net_amount"));
        p.setRemarks(rs.getString("remarks"));
        return p;
    }
}
