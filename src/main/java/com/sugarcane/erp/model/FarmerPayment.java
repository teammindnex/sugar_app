package com.sugarcane.erp.model;

import java.time.LocalDate;

public class FarmerPayment {
    private int id;
    private int farmerId;
    private LocalDate paymentDate;
    private double amount;
    private String paymentMode;
    private String refNo;

    public FarmerPayment() {}

    public FarmerPayment(int farmerId, LocalDate paymentDate, double amount, String paymentMode, String refNo) {
        this.farmerId = farmerId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.refNo = refNo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getRefNo() { return refNo; }
    public void setRefNo(String refNo) { this.refNo = refNo; }
}
