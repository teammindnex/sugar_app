package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.Transport;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransportDAO {

    public void addTransport(Transport transport) throws SQLException {
        String sql = "INSERT INTO Transports (transport_name, vehicle_no, driver_name, driver_mobile, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, transport.getTransportName());
            pstmt.setString(2, transport.getVehicleNo());
            pstmt.setString(3, transport.getDriverName());
            pstmt.setString(4, transport.getDriverMobile());
            pstmt.setString(5, transport.getStatus());
            
            pstmt.executeUpdate();
        }
    }

    public List<Transport> getAllTransports() throws SQLException {
        List<Transport> list = new ArrayList<>();
        String sql = "SELECT * FROM Transports ORDER BY transport_name ASC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToTransport(rs));
            }
        }
        return list;
    }
    
    public void updateTransport(Transport transport) throws SQLException {
        String sql = "UPDATE Transports SET transport_name=?, vehicle_no=?, driver_name=?, driver_mobile=?, " +
                     "status=?, updated_at=CURRENT_TIMESTAMP " +
                     "WHERE id=?";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, transport.getTransportName());
            pstmt.setString(2, transport.getVehicleNo());
            pstmt.setString(3, transport.getDriverName());
            pstmt.setString(4, transport.getDriverMobile());
            pstmt.setString(5, transport.getStatus());
            pstmt.setInt(6, transport.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    public void deleteTransport(int id) throws SQLException {
        String sql = "DELETE FROM Transports WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Transport mapResultSetToTransport(ResultSet rs) throws SQLException {
        return new Transport(
            rs.getInt("id"),
            rs.getString("transport_name"),
            rs.getString("vehicle_no"),
            rs.getString("driver_name"),
            rs.getString("driver_mobile"),
            rs.getString("status")
        );
    }
}
