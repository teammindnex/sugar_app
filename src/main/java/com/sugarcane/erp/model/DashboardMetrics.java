package com.sugarcane.erp.model;

public class DashboardMetrics {
    private double todayPurchase;
    private double todaySales;
    private double todayExpenses;
    private double todayCollection;
    private double todayPayments;
    private int totalFarmers;
    private int totalCustomers;
    private int totalWorkers;
    private int totalVehicles;
    
    private double monthlyPurchaseWeight;
    private double monthlySalesWeight;
    private double yearlyPurchaseWeight;
    private double yearlySalesWeight;

    public double getTodayPurchase() { return todayPurchase; }
    public void setTodayPurchase(double todayPurchase) { this.todayPurchase = todayPurchase; }

    public double getTodaySales() { return todaySales; }
    public void setTodaySales(double todaySales) { this.todaySales = todaySales; }

    public double getTodayExpenses() { return todayExpenses; }
    public void setTodayExpenses(double todayExpenses) { this.todayExpenses = todayExpenses; }

    public double getTodayCollection() { return todayCollection; }
    public void setTodayCollection(double todayCollection) { this.todayCollection = todayCollection; }

    public double getTodayPayments() { return todayPayments; }
    public void setTodayPayments(double todayPayments) { this.todayPayments = todayPayments; }

    public int getTotalFarmers() { return totalFarmers; }
    public void setTotalFarmers(int totalFarmers) { this.totalFarmers = totalFarmers; }

    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }

    public int getTotalWorkers() { return totalWorkers; }
    public void setTotalWorkers(int totalWorkers) { this.totalWorkers = totalWorkers; }

    public int getTotalVehicles() { return totalVehicles; }
    public void setTotalVehicles(int totalVehicles) { this.totalVehicles = totalVehicles; }

    public double getMonthlyPurchaseWeight() { return monthlyPurchaseWeight; }
    public void setMonthlyPurchaseWeight(double monthlyPurchaseWeight) { this.monthlyPurchaseWeight = monthlyPurchaseWeight; }

    public double getMonthlySalesWeight() { return monthlySalesWeight; }
    public void setMonthlySalesWeight(double monthlySalesWeight) { this.monthlySalesWeight = monthlySalesWeight; }

    public double getYearlyPurchaseWeight() { return yearlyPurchaseWeight; }
    public void setYearlyPurchaseWeight(double yearlyPurchaseWeight) { this.yearlyPurchaseWeight = yearlyPurchaseWeight; }

    public double getYearlySalesWeight() { return yearlySalesWeight; }
    public void setYearlySalesWeight(double yearlySalesWeight) { this.yearlySalesWeight = yearlySalesWeight; }
}
