package com.sugarcane.erp.model;

import java.time.LocalDate;

public class Expense {
    private int id;
    private LocalDate expenseDate;
    private String category;
    private double amount;
    private String description;
    private String paymentMode;

    public Expense() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
}
