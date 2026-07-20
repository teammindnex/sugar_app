package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.Worker;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerDAO {

    public void addWorker(Worker worker) throws SQLException {
        String sql = "INSERT INTO Workers (name, mobile, village, work_type, joining_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, worker.getName());
            pstmt.setString(2, worker.getMobile());
            pstmt.setString(3, worker.getVillage());
            pstmt.setString(4, worker.getWorkType());
            pstmt.setDate(5, worker.getJoiningDate() != null ? Date.valueOf(worker.getJoiningDate()) : null);
            pstmt.setString(6, worker.getStatus());
            
            pstmt.executeUpdate();
        }
    }

    public List<Worker> getAllWorkers() throws SQLException {
        List<Worker> list = new ArrayList<>();
        String sql = "SELECT * FROM Workers ORDER BY name ASC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToWorker(rs));
            }
        }
        return list;
    }
    
    public void updateWorker(Worker worker) throws SQLException {
        String sql = "UPDATE Workers SET name=?, mobile=?, village=?, work_type=?, joining_date=?, " +
                     "status=?, updated_at=CURRENT_TIMESTAMP " +
                     "WHERE id=?";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, worker.getName());
            pstmt.setString(2, worker.getMobile());
            pstmt.setString(3, worker.getVillage());
            pstmt.setString(4, worker.getWorkType());
            pstmt.setDate(5, worker.getJoiningDate() != null ? Date.valueOf(worker.getJoiningDate()) : null);
            pstmt.setString(6, worker.getStatus());
            pstmt.setInt(7, worker.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    public void deleteWorker(int id) throws SQLException {
        String sql = "DELETE FROM Workers WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Worker mapResultSetToWorker(ResultSet rs) throws SQLException {
        Date sqlDate = rs.getDate("joining_date");
        return new Worker(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("mobile"),
            rs.getString("village"),
            rs.getString("work_type"),
            sqlDate != null ? sqlDate.toLocalDate() : null,
            rs.getString("status")
        );
    }
}
