package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.FarmerPayment;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FarmerPaymentDAO {

    public void addPayment(FarmerPayment payment) throws SQLException {
        String sql = "INSERT INTO Farmer_Payments (farmer_id, payment_date, amount, payment_mode, ref_no) " +
                     "VALUES (?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, payment.getFarmerId());
            pstmt.setDate(2, Date.valueOf(payment.getPaymentDate()));
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, payment.getPaymentMode());
            pstmt.setString(5, payment.getRefNo());
            
            pstmt.executeUpdate();
        }
    }
}
