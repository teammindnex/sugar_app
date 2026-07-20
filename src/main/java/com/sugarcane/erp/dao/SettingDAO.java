package com.sugarcane.erp.dao;

import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingDAO {

    public String getSetting(String key) throws SQLException {
        String sql = "SELECT setting_value FROM Settings WHERE setting_key = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("setting_value");
            }
        }
        return null;
    }

    public void updateSetting(String key, String value) throws SQLException {
        String sql = "UPDATE Settings SET setting_value = ? WHERE setting_key = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setString(2, key);
            pstmt.executeUpdate();
        }
    }
}
