package com.sugarcane.erp.model;

import java.time.LocalDate;

public class LedgerEntry {
    private LocalDate date;
    private String billNo;
    private String particulars;
    private String caneType;
    private double emptyWeight;
    private double loadedWeight;
    private double weight;
    private double debit;   // Amount we owe them or they owe us
    private double credit;  // Amount paid
    private double balance;

    public LedgerEntry(LocalDate date, String billNo, String particulars, String caneType, double emptyWeight, double loadedWeight, double weight, double debit, double credit) {
        this.date = date;
        this.billNo = billNo;
        this.particulars = particulars;
        this.caneType = caneType;
        this.emptyWeight = emptyWeight;
        this.loadedWeight = loadedWeight;
        this.weight = weight;
        this.debit = debit;
        this.credit = credit;
    }

    public LedgerEntry(LocalDate date, String particulars, String caneType, double weight, double debit, double credit) {
        this(date, "", particulars, caneType, 0, 0, weight, debit, credit);
    }

    public LocalDate getDate() { return date; }
    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }
    public String getParticulars() { return particulars; }
    public String getCaneType() { return caneType; }
    public void setCaneType(String caneType) { this.caneType = caneType; }

    public double getEmptyWeight() { return emptyWeight; }
    public void setEmptyWeight(double emptyWeight) { this.emptyWeight = emptyWeight; }

    public double getLoadedWeight() { return loadedWeight; }
    public void setLoadedWeight(double loadedWeight) { this.loadedWeight = loadedWeight; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public double getDebit() { return debit; }
    public double getCredit() { return credit; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
