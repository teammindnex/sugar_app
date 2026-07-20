package com.sugarcane.erp.dao;

import com.sugarcane.erp.model.Expense;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public void addExpense(Expense expense) throws SQLException {
        String sql = "INSERT INTO Expenses (expense_date, category, amount, description, payment_mode) VALUES (?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(expense.getExpenseDate()));
            pstmt.setString(2, expense.getCategory());
            pstmt.setDouble(3, expense.getAmount());
            pstmt.setString(4, expense.getDescription());
            pstmt.setString(5, expense.getPaymentMode());
            
            pstmt.executeUpdate();
        }
    }

    public List<Expense> getAllExpenses() throws SQLException {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM Expenses ORDER BY expense_date DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToExpense(rs));
            }
        }
        return list;
    }
    
    public void deleteExpense(int id) throws SQLException {
        String sql = "DELETE FROM Expenses WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getInt("id"));
        e.setExpenseDate(rs.getDate("expense_date").toLocalDate());
        e.setCategory(rs.getString("category"));
        e.setAmount(rs.getDouble("amount"));
        e.setDescription(rs.getString("description"));
        e.setPaymentMode(rs.getString("payment_mode"));
        return e;
    }
}
