package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.ExpenseDAO;
import com.sugarcane.erp.model.Expense;

import java.sql.SQLException;
import java.util.List;

public class ExpenseService {

    private final ExpenseDAO expenseDAO;

    public ExpenseService() {
        this.expenseDAO = new ExpenseDAO();
    }

    public void addExpense(Expense expense) throws SQLException {
        expenseDAO.addExpense(expense);
    }

    public List<Expense> getAllExpenses() throws SQLException {
        return expenseDAO.getAllExpenses();
    }

    public void deleteExpense(int id) throws SQLException {
        expenseDAO.deleteExpense(id);
    }
}
