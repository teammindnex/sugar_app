package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.TransportTrip;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransportTripDAO {

    public void addTrip(TransportTrip trip) throws SQLException {
        String sql = "INSERT INTO Transport_Trips (transport_id, trip_date, farmer_id, customer_id, pickup_location, " +
                     "destination, weight, trip_charge, diesel, toll, advance, balance, trip_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, trip.getTransportId());
            pstmt.setDate(2, Date.valueOf(trip.getTripDate()));
            
            if (trip.getFarmerId() != null) pstmt.setInt(3, trip.getFarmerId());
            else pstmt.setNull(3, Types.INTEGER);
            
            if (trip.getCustomerId() != null) pstmt.setInt(4, trip.getCustomerId());
            else pstmt.setNull(4, Types.INTEGER);
            
            pstmt.setString(5, trip.getPickupLocation());
            pstmt.setString(6, trip.getDestination());
            pstmt.setDouble(7, trip.getWeight());
            pstmt.setDouble(8, trip.getTripCharge());
            pstmt.setDouble(9, trip.getDiesel());
            pstmt.setDouble(10, trip.getToll());
            pstmt.setDouble(11, trip.getAdvance());
            pstmt.setDouble(12, trip.getBalance());
            pstmt.setString(13, trip.getTripStatus());
            
            pstmt.executeUpdate();
        }
    }

    public List<TransportTrip> getAllTrips() throws SQLException {
        List<TransportTrip> list = new ArrayList<>();
        String sql = "SELECT t.*, tr.vehicle_no as transport_name FROM Transport_Trips t " +
                     "JOIN Transports tr ON t.transport_id = tr.id ORDER BY t.trip_date DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToTrip(rs));
            }
        }
        return list;
    }
    
    public void deleteTrip(int id) throws SQLException {
        String sql = "DELETE FROM Transport_Trips WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private TransportTrip mapResultSetToTrip(ResultSet rs) throws SQLException {
        TransportTrip t = new TransportTrip();
        t.setId(rs.getInt("id"));
        t.setTransportId(rs.getInt("transport_id"));
        t.setTransportName(rs.getString("transport_name"));
        t.setTripDate(rs.getDate("trip_date").toLocalDate());
        
        int farmerId = rs.getInt("farmer_id");
        if (!rs.wasNull()) t.setFarmerId(farmerId);
        
        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) t.setCustomerId(customerId);
        
        t.setPickupLocation(rs.getString("pickup_location"));
        t.setDestination(rs.getString("destination"));
        t.setWeight(rs.getDouble("weight"));
        t.setTripCharge(rs.getDouble("trip_charge"));
        t.setDiesel(rs.getDouble("diesel"));
        t.setToll(rs.getDouble("toll"));
        t.setAdvance(rs.getDouble("advance"));
        t.setBalance(rs.getDouble("balance"));
        t.setTripStatus(rs.getString("trip_status"));
        return t;
    }
}
