package com.sugarcane.erp.model;

import java.time.LocalDate;

public class LedgerEntry {
    private LocalDate date;
    private String particulars;
    private double debit;   // Amount we owe them or they owe us
    private double credit;  // Amount paid
    private double balance;

    public LedgerEntry(LocalDate date, String particulars, double debit, double credit) {
        this.date = date;
        this.particulars = particulars;
        this.debit = debit;
        this.credit = credit;
    }

    public LocalDate getDate() { return date; }
    public String getParticulars() { return particulars; }
    public double getDebit() { return debit; }
    public double getCredit() { return credit; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
