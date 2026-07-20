package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.WorkerDailyEntry;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerDailyEntryDAO {

    public void addEntry(WorkerDailyEntry entry) throws SQLException {
        String sql = "INSERT INTO Worker_Daily_Entries (worker_id, entry_date, attendance, bundles, rate_per_bundle, " +
                     "total_salary, bonus, advance, penalty, tea_expense, food_expense, other_expense, net_salary) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, entry.getWorkerId());
            pstmt.setDate(2, Date.valueOf(entry.getEntryDate()));
            pstmt.setString(3, entry.getAttendance());
            pstmt.setInt(4, entry.getBundles());
            pstmt.setDouble(5, entry.getRatePerBundle());
            pstmt.setDouble(6, entry.getTotalSalary());
            pstmt.setDouble(7, entry.getBonus());
            pstmt.setDouble(8, entry.getAdvance());
            pstmt.setDouble(9, entry.getPenalty());
            pstmt.setDouble(10, entry.getTeaExpense());
            pstmt.setDouble(11, entry.getFoodExpense());
            pstmt.setDouble(12, entry.getOtherExpense());
            pstmt.setDouble(13, entry.getNetSalary());
            
            pstmt.executeUpdate();
        }
    }

    public List<WorkerDailyEntry> getAllEntries() throws SQLException {
        List<WorkerDailyEntry> list = new ArrayList<>();
        String sql = "SELECT e.*, w.name as worker_name FROM Worker_Daily_Entries e " +
                     "JOIN Workers w ON e.worker_id = w.id ORDER BY e.entry_date DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToEntry(rs));
            }
        }
        return list;
    }
    
    public void deleteEntry(int id) throws SQLException {
        String sql = "DELETE FROM Worker_Daily_Entries WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private WorkerDailyEntry mapResultSetToEntry(ResultSet rs) throws SQLException {
        WorkerDailyEntry e = new WorkerDailyEntry();
        e.setId(rs.getInt("id"));
        e.setWorkerId(rs.getInt("worker_id"));
        e.setWorkerName(rs.getString("worker_name"));
        e.setEntryDate(rs.getDate("entry_date").toLocalDate());
        e.setAttendance(rs.getString("attendance"));
        e.setBundles(rs.getInt("bundles"));
        e.setRatePerBundle(rs.getDouble("rate_per_bundle"));
        e.setTotalSalary(rs.getDouble("total_salary"));
        e.setBonus(rs.getDouble("bonus"));
        e.setAdvance(rs.getDouble("advance"));
        e.setPenalty(rs.getDouble("penalty"));
        e.setTeaExpense(rs.getDouble("tea_expense"));
        e.setFoodExpense(rs.getDouble("food_expense"));
        e.setOtherExpense(rs.getDouble("other_expense"));
        e.setNetSalary(rs.getDouble("net_salary"));
        return e;
    }
}
