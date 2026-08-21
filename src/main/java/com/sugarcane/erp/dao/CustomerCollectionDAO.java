package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.CustomerCollection;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerCollectionDAO {

    public void addCollection(CustomerCollection collection) throws SQLException {
        String sql = "INSERT INTO Customer_Collections (customer_id, collection_date, amount, payment_mode, ref_no) " +
                     "VALUES (?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, collection.getCustomerId());
            pstmt.setDate(2, Date.valueOf(collection.getCollectionDate()));
            pstmt.setDouble(3, collection.getAmount());
            pstmt.setString(4, collection.getPaymentMode());
            pstmt.setString(5, collection.getRefNo());
            
            pstmt.executeUpdate();
        }
    }
}
