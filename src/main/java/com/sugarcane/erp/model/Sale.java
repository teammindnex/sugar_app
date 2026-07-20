package com.sugarcane.erp.model;

import java.time.LocalDate;

public class Sale {
    private int id;
    private int customerId;
    private String customerName; // Transient
    private LocalDate saleDate;
    private String caneType;
    private String vehicleNo;
    private double weight;
    private double ratePerTon;
    private double totalAmount;
    private double receivedAmount;
    private double netAmount;
    private String remarks;

    public Sale() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public String getCaneType() { return caneType; }
    public void setCaneType(String caneType) { this.caneType = caneType; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getRatePerTon() { return ratePerTon; }
    public void setRatePerTon(double ratePerTon) { this.ratePerTon = ratePerTon; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(double receivedAmount) { this.receivedAmount = receivedAmount; }

    public double getNetAmount() { return netAmount; }
    public void setNetAmount(double netAmount) { this.netAmount = netAmount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
