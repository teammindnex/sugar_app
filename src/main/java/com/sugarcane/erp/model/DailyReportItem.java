package com.sugarcane.erp.model;

import java.time.LocalDate;

public class DailyReportItem {
    private LocalDate reportDate;
    private double totalPurchaseWeight;
    private double totalPurchaseAmount;
    private double totalSaleWeight;
    private double totalSaleAmount;

    public DailyReportItem(LocalDate reportDate, double totalPurchaseWeight, double totalPurchaseAmount, double totalSaleWeight, double totalSaleAmount) {
        this.reportDate = reportDate;
        this.totalPurchaseWeight = totalPurchaseWeight;
        this.totalPurchaseAmount = totalPurchaseAmount;
        this.totalSaleWeight = totalSaleWeight;
        this.totalSaleAmount = totalSaleAmount;
    }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    
    public double getTotalPurchaseWeight() { return totalPurchaseWeight; }
    public void setTotalPurchaseWeight(double totalPurchaseWeight) { this.totalPurchaseWeight = totalPurchaseWeight; }
    
    public double getTotalPurchaseAmount() { return totalPurchaseAmount; }
    public void setTotalPurchaseAmount(double totalPurchaseAmount) { this.totalPurchaseAmount = totalPurchaseAmount; }
    
    public double getTotalSaleWeight() { return totalSaleWeight; }
    public void setTotalSaleWeight(double totalSaleWeight) { this.totalSaleWeight = totalSaleWeight; }
    
    public double getTotalSaleAmount() { return totalSaleAmount; }
    public void setTotalSaleAmount(double totalSaleAmount) { this.totalSaleAmount = totalSaleAmount; }
}
