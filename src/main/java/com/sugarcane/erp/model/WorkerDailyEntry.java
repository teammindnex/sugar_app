package com.sugarcane.erp.model;

import java.time.LocalDate;

public class WorkerDailyEntry {
    private int id;
    private int workerId;
    private String workerName; // Transient for UI display
    private LocalDate entryDate;
    private String attendance; // PRESENT, ABSENT, HALF_DAY
    private int bundles;
    private double ratePerBundle;
    private double totalSalary;
    private double bonus;
    private double advance;
    private double penalty;
    private double teaExpense;
    private double foodExpense;
    private double otherExpense;
    private double netSalary;

    public WorkerDailyEntry() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWorkerId() { return workerId; }
    public void setWorkerId(int workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public String getAttendance() { return attendance; }
    public void setAttendance(String attendance) { this.attendance = attendance; }

    public int getBundles() { return bundles; }
    public void setBundles(int bundles) { this.bundles = bundles; }

    public double getRatePerBundle() { return ratePerBundle; }
    public void setRatePerBundle(double ratePerBundle) { this.ratePerBundle = ratePerBundle; }

    public double getTotalSalary() { return totalSalary; }
    public void setTotalSalary(double totalSalary) { this.totalSalary = totalSalary; }

    public double getBonus() { return bonus; }
    public void setBonus(double bonus) { this.bonus = bonus; }

    public double getAdvance() { return advance; }
    public void setAdvance(double advance) { this.advance = advance; }

    public double getPenalty() { return penalty; }
    public void setPenalty(double penalty) { this.penalty = penalty; }

    public double getTeaExpense() { return teaExpense; }
    public void setTeaExpense(double teaExpense) { this.teaExpense = teaExpense; }

    public double getFoodExpense() { return foodExpense; }
    public void setFoodExpense(double foodExpense) { this.foodExpense = foodExpense; }

    public double getOtherExpense() { return otherExpense; }
    public void setOtherExpense(double otherExpense) { this.otherExpense = otherExpense; }

    public double getNetSalary() { return netSalary; }
    public void setNetSalary(double netSalary) { this.netSalary = netSalary; }
}
