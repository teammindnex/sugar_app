package com.sugarcane.erp.model;

import java.time.LocalDate;

public class Purchase {
    private int id;
    private int farmerId;
    private String farmerName; // Transient for UI display
    private LocalDate purchaseDate;
    private String caneType;
    private String vehicleNo;
    private double weight;
    private double ratePerTon;
    private double totalAmount;
    private double advance;
    private double loadingCharges;
    private double cuttingCharges;
    private double transportCharges;
    private double otherCharges;
    private double netAmount;
    private String remarks;

    public Purchase() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

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

    public double getAdvance() { return advance; }
    public void setAdvance(double advance) { this.advance = advance; }

    public double getLoadingCharges() { return loadingCharges; }
    public void setLoadingCharges(double loadingCharges) { this.loadingCharges = loadingCharges; }

    public double getCuttingCharges() { return cuttingCharges; }
    public void setCuttingCharges(double cuttingCharges) { this.cuttingCharges = cuttingCharges; }

    public double getTransportCharges() { return transportCharges; }
    public void setTransportCharges(double transportCharges) { this.transportCharges = transportCharges; }

    public double getOtherCharges() { return otherCharges; }
    public void setOtherCharges(double otherCharges) { this.otherCharges = otherCharges; }

    public double getNetAmount() { return netAmount; }
    public void setNetAmount(double netAmount) { this.netAmount = netAmount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
